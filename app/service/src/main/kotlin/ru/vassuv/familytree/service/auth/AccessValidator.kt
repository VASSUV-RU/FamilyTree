package ru.vassuv.familytree.service.auth

import org.springframework.stereotype.Service
import ru.vassuv.familytree.config.exception.UnauthorizeException
import ru.vassuv.familytree.data.auth.session.SessionCache
import ru.vassuv.familytree.data.auth.session.SessionJpaRepository
import ru.vassuv.familytree.data.auth.session.SessionStatus
import ru.vassuv.familytree.data.auth.session.BlocklistCache
import java.time.Instant

data class ValidatedSession(
    val userId: Long,
    val jti: String,
    val activeFamilyId: Long?,
    val scopes: String?,
)

@Service
class AccessValidator(
    private val jwt: JwtService,
    private val sessionCache: SessionCache,
    private val sessionRepo: SessionJpaRepository,
    private val blocklist: BlocklistCache,
) {
    fun validate(accessJwt: String): ValidatedSession {
        val decoded = try { jwt.verifyAndDecode(accessJwt) } catch (e: Exception) {
            throw UnauthorizeException()
        }
        val jti = decoded.jti ?: throw UnauthorizeException()
        if (blocklist.isBlocked(jti)) throw UnauthorizeException()

        val cached = sessionCache.get(jti)
        if (cached != null) {
            return ValidatedSession(
                userId = cached.userId,
                jti = cached.jti,
                activeFamilyId = cached.activeFamilyId,
                scopes = cached.scopes,
            )
        }

        val entity = sessionRepo.findById(jti).orElseThrow { UnauthorizeException() }
        if (entity.status != SessionStatus.ACTIVE) throw UnauthorizeException()
        if (Instant.now().isAfter(entity.expiresAt)) throw UnauthorizeException()

        // Rehydrate cache
        sessionCache.put(
            ru.vassuv.familytree.data.auth.session.CachedSession(
                jti = entity.jti,
                userId = entity.userId,
                activeFamilyId = entity.activeFamilyId,
                scopes = entity.scopes,
                capVersion = entity.capVersion,
            ),
            ttlSeconds = (entity.expiresAt.epochSecond - Instant.now().epochSecond).coerceAtLeast(1)
        )

        return ValidatedSession(
            userId = entity.userId,
            jti = entity.jti,
            activeFamilyId = entity.activeFamilyId,
            scopes = entity.scopes,
        )
    }
}

