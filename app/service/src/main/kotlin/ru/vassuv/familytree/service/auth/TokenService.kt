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
}

@Service
class DefaultTokenService(
    private val sessionRepo: SessionJpaRepository,
    private val refreshRepo: RefreshTokenJpaRepository,
    private val sessionCache: SessionCache,
    private val blocklist: BlocklistCache,
    private val jwt: JwtService,
    private val props: ru.vassuv.familytree.config.JwtProperties,
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

        val session = SessionEntity(
            jti = jti,
            userId = userId,
            activeFamilyId = null, // TODO: определить по invitationId (если есть)
            scopes = null,
            capVersion = 1,
            issuedAt = now,
            expiresAt = now.plus(accessTtl),
            status = SessionStatus.ACTIVE,
            meta = null,
        )
        sessionRepo.save(session)

        // Cache session for quick checks
        sessionCache.put(
            ru.vassuv.familytree.data.auth.session.CachedSession(
                jti = jti,
                userId = userId,
                activeFamilyId = null,
                scopes = null,
                capVersion = 1,
            ),
            ttlSeconds = accessTtl.seconds
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
}
