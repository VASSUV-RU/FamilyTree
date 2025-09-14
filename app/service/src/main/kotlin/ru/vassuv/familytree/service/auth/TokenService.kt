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
) : TokenService {
    private val logger = LoggerFactory.getLogger(javaClass)

    // TODO(ft-auth-05): вынести TTL и issuer в JwtProperties (config) и использовать реальный JWT RS256
    private val accessTtl: Duration = Duration.ofMinutes(15)
    private val refreshTtl: Duration = Duration.ofDays(30)

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

        // Placeholder access token; replace with JWT signed token
        val access = "access-$jti"
        logger.debug("Issued tokens for telegramId={}, jti={}", telegramId, jti)
        return AuthTokens(accessToken = access, refreshToken = rid)
    }
}
