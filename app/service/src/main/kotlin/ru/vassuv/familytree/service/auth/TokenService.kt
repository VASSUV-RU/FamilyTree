package ru.vassuv.familytree.service.auth

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.vassuv.familytree.data.auth.session.BlocklistCache
import ru.vassuv.familytree.data.auth.session.RefreshTokenEntity
import ru.vassuv.familytree.data.auth.session.RefreshTokenJpaRepository
import ru.vassuv.familytree.data.auth.session.SessionCache
import ru.vassuv.familytree.data.auth.session.SessionEntity
import ru.vassuv.familytree.data.auth.session.SessionJpaRepository
import ru.vassuv.familytree.data.auth.session.SessionStatus
import ru.vassuv.familytree.service.model.AuthTokens
import java.time.Duration
import java.time.Instant
import java.util.UUID

interface TokenService {
    fun issueForTelegram(telegramId: Long, invitationId: String?): AuthTokens
    fun refresh(refreshRid: String): AuthTokens
    fun logout(jti: String)
    fun switchActiveFamily(userId: Long, currentJti: String, familyId: Long): AuthTokens
}

@Service
class DefaultTokenService(
    private val sessionRepo: SessionJpaRepository,
    private val refreshRepo: RefreshTokenJpaRepository,
    private val sessionCache: SessionCache,
    private val blocklist: BlocklistCache,
    private val jwt: JwtService,
    private val props: ru.vassuv.familytree.config.JwtProperties,
    private val invitation: ru.vassuv.familytree.service.invite.InvitationService,
) : TokenService {
    private val logger = LoggerFactory.getLogger(javaClass)

    // TTL берём из конфига (JwtProperties)
    private val accessTtl: Duration get() = Duration.ofSeconds(props.accessTtlSeconds)
    private val refreshTtl: Duration get() = Duration.ofSeconds(props.refreshTtlSeconds)

    override fun issueForTelegram(telegramId: Long, invitationId: String?): AuthTokens {
        val now = Instant.now()
        val jti = UUID.randomUUID().toString()
        val rid = UUID.randomUUID().toString()

        // TODO(ft-auth-05): найти/создать userId по telegramId
        val userId = telegramId

        // accept invitation if provided and resolve activeFamilyId
        val resolvedFamilyId: Long? = invitationId?.let {
            runCatching { invitation.accept(it, telegramId) }
                .onFailure { logger.warn("Invitation accept failed: id={}, tg={}, err={}", it, telegramId, it.message) }
                .getOrNull()
        }

        val session = SessionEntity(
            jti = jti,
            userId = userId,
            activeFamilyId = resolvedFamilyId,
            scopes = null,
            capVersion = 1,
            issuedAt = now,
            expiresAt = now.plus(accessTtl),
            status = SessionStatus.ACTIVE,
            meta = null,
        )
        sessionRepo.save(session)

        // Cache session for quick checks (add small safety delta ≈ +30s)
        sessionCache.put(
            ru.vassuv.familytree.data.auth.session.CachedSession(
                jti = jti,
                userId = userId,
                activeFamilyId = resolvedFamilyId,
                scopes = null,
                capVersion = 1,
            ),
            ttlSeconds = accessTtl.seconds + 30
        )

        // Create refresh token record
        refreshRepo.save(
            RefreshTokenEntity(
                rid = rid,
                userId = userId,
                sessionJti = jti,
                issuedAt = now,
                expiresAt = now.plus(refreshTtl),
            )
        )

        // Реальный access JWT (RS256)
        val access = jwt.sign(
            subject = userId.toString(),
            jti = jti,
            expiresAt = now.plus(accessTtl),
            extraClaims = emptyMap(),
        )
        logger.debug("Issued tokens for telegramId={}, jti={}", telegramId, jti)
        return AuthTokens(accessToken = access, refreshToken = rid)
    }

    override fun refresh(refreshRid: String): AuthTokens {
        val now = Instant.now()
        val refreshOpt = refreshRepo.findById(refreshRid)
        val refresh = refreshOpt.orElseThrow { ru.vassuv.familytree.config.exception.UnauthorizeException() }
        if (refresh.expiresAt.isBefore(now) || refresh.status != ru.vassuv.familytree.data.auth.session.RefreshStatus.ACTIVE || refresh.revokedAt != null) {
            throw ru.vassuv.familytree.config.exception.UnauthorizeException()
        }

        val oldJti = refresh.sessionJti
        val oldSession = sessionRepo.findById(oldJti).orElse(null)
        if (oldSession == null) {
            // Без старой сессии продолжать нельзя — отказ
            throw ru.vassuv.familytree.config.exception.UnauthorizeException()
        }

        // Заблокировать старый jti на остаток жизни access токена (минимум 1с)
        val blkTtl = (oldSession.expiresAt.epochSecond - now.epochSecond).coerceAtLeast(1)
        runCatching { blocklist.block(oldJti, blkTtl.toLong(), reason = "rotated") }

        // Создать новую серверную сессию с новым jti
        val newJti = UUID.randomUUID().toString()
        val newSession = SessionEntity(
            jti = newJti,
            userId = oldSession.userId,
            activeFamilyId = oldSession.activeFamilyId,
            scopes = oldSession.scopes,
            capVersion = oldSession.capVersion,
            issuedAt = now,
            expiresAt = now.plus(accessTtl),
            status = SessionStatus.ACTIVE,
            meta = oldSession.meta,
        )
        sessionRepo.save(newSession)

        // Обновить кеш сессии (с небольшим дельта-запасом)
        sessionCache.put(
            ru.vassuv.familytree.data.auth.session.CachedSession(
                jti = newJti,
                userId = newSession.userId,
                activeFamilyId = newSession.activeFamilyId,
                scopes = newSession.scopes,
                capVersion = newSession.capVersion,
            ),
            ttlSeconds = accessTtl.seconds + 30
        )

        // Обновить привязку refresh к новой сессии (ротируем jti)
        val updatedRefresh = refresh.copy(sessionJti = newJti, rotatedAt = now)
        refreshRepo.save(updatedRefresh)

        // Сформировать новый access JWT
        val access = jwt.sign(
            subject = newSession.userId.toString(),
            jti = newJti,
            expiresAt = newSession.expiresAt,
        )
        logger.debug("Refreshed tokens for rid={}, oldJti={}, newJti={}", refreshRid, oldJti, newJti)
        return AuthTokens(accessToken = access, refreshToken = refreshRid)
    }

    override fun logout(jti: String) {
        val now = Instant.now()
        // Try to determine TTL from DB session, fallback to access TTL
        val ttl = sessionRepo.findById(jti).map { entity ->
            (entity.expiresAt.epochSecond - now.epochSecond).coerceAtLeast(1)
        }.orElse(accessTtl.seconds).coerceAtLeast(1)

        // Block old jti and evict cache
        runCatching { blocklist.block(jti, ttl, reason = "logout") }
        runCatching { sessionCache.evict(jti) }
        // Optionally mark DB session as revoked (best-effort)
        runCatching {
            val entity = sessionRepo.findById(jti).orElse(null)
            if (entity != null && entity.status != SessionStatus.REVOKED) {
                sessionRepo.save(entity.copy(status = SessionStatus.REVOKED))
            }
        }
        logger.debug("Logged out session jti={}, ttl={}s", jti, ttl)
    }

    override fun switchActiveFamily(userId: Long, currentJti: String, familyId: Long): AuthTokens {
        require(familyId > 0) { "familyId must be positive" }
        val now = Instant.now()
        val entity = sessionRepo.findById(currentJti).orElseThrow { ru.vassuv.familytree.config.exception.UnauthorizeException() }
        if (entity.userId != userId) throw ru.vassuv.familytree.config.exception.UnauthorizeException()

        // TODO(ft-auth-08): проверить членство userId в familyId и вычислить scopes из БД
        val scopes: String? = entity.scopes // оставляем без изменений до подключения ролей

        val updated = entity.copy(
            activeFamilyId = familyId,
            scopes = scopes,
            capVersion = entity.capVersion + 1,
            // продлеваем access согласно конфигу (новый выпуск)
            issuedAt = now,
            expiresAt = now.plus(accessTtl),
        )
        sessionRepo.save(updated)

        // Обновить кеш
        sessionCache.put(
            ru.vassuv.familytree.data.auth.session.CachedSession(
                jti = updated.jti,
                userId = updated.userId,
                activeFamilyId = updated.activeFamilyId,
                scopes = updated.scopes,
                capVersion = updated.capVersion,
            ),
            ttlSeconds = accessTtl.seconds + 30
        )

        // Сформировать новый access с тем же jti
        val access = jwt.sign(
            subject = updated.userId.toString(),
            jti = updated.jti,
            expiresAt = updated.expiresAt,
        )

        // Выбрать действующий refresh для ответа
        val rid = refreshRepo.findAllBySessionJti(currentJti)
            .filter { it.status == ru.vassuv.familytree.data.auth.session.RefreshStatus.ACTIVE && it.expiresAt.isAfter(now) }
            .maxByOrNull { it.expiresAt }?.rid
            ?: run {
                // Если по какой-то причине нет refresh — создать новый
                val newRid = java.util.UUID.randomUUID().toString()
                refreshRepo.save(
                    RefreshTokenEntity(
                        rid = newRid,
                        userId = updated.userId,
                        sessionJti = updated.jti,
                        issuedAt = now,
                        expiresAt = now.plus(refreshTtl),
                    )
                )
                newRid
            }

        return AuthTokens(accessToken = access, refreshToken = rid)
    }
}
