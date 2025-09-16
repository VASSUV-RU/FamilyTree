package ru.vassuv.familytree.service.auth

import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import ru.vassuv.familytree.config.JwtProperties
import ru.vassuv.familytree.config.exception.UnauthorizeException
import ru.vassuv.familytree.data.auth.session.BlocklistCache
import ru.vassuv.familytree.data.auth.session.RefreshStatus
import ru.vassuv.familytree.data.auth.session.RefreshTokenEntity
import ru.vassuv.familytree.data.auth.session.RefreshTokenJpaRepository
import ru.vassuv.familytree.data.auth.session.SessionEntity
import ru.vassuv.familytree.data.auth.session.SessionJpaRepository
import ru.vassuv.familytree.data.auth.session.SessionStatus
import ru.vassuv.familytree.service.auth.audit.AuthAuditService
import java.time.Instant
import java.util.Optional

class TokenServiceRefreshTest {

    private fun newService(
        sessionRepo: SessionJpaRepository,
        refreshRepo: RefreshTokenJpaRepository,
        blocklist: BlocklistCache,
    ): Pair<DefaultTokenService, JwtService> {
        val cache: ru.vassuv.familytree.data.auth.session.SessionCache = mock()
        val jwt = JwtService(JwtProperties())
        val invitation: ru.vassuv.familytree.service.invite.InvitationService = mock()
        val audit: AuthAuditService = mock()
        val svc = DefaultTokenService(sessionRepo, refreshRepo, cache, blocklist, jwt, JwtProperties(), invitation, audit)
        return Pair(svc, jwt)
    }

    @Test
    fun `refresh rotates jti and blocks old`() {
        val sessionRepo: SessionJpaRepository = mock()
        val refreshRepo: RefreshTokenJpaRepository = mock()
        val blocklist: BlocklistCache = mock()

        val rid = "R1"
        val oldJti = "J-OLD"
        val now = Instant.now()

        val refresh = RefreshTokenEntity(
            rid = rid,
            userId = 77,
            sessionJti = oldJti,
            issuedAt = now.minusSeconds(100),
            expiresAt = now.plusSeconds(300),
            status = RefreshStatus.ACTIVE,
        )
        whenever(refreshRepo.findById(eq(rid))).thenReturn(Optional.of(refresh))

        val oldSession = SessionEntity(
            jti = oldJti,
            userId = 77,
            expiresAt = now.plusSeconds(120),
            status = SessionStatus.ACTIVE,
        )
        whenever(sessionRepo.findById(eq(oldJti))).thenReturn(Optional.of(oldSession))
        whenever(sessionRepo.save(any())).thenAnswer { it.getArgument<SessionEntity>(0) }
        whenever(refreshRepo.save(any())).thenAnswer { it.getArgument<RefreshTokenEntity>(0) }

        val (svc, jwt) = newService(sessionRepo, refreshRepo, blocklist)
        val tokens = svc.refresh(rid)

        // decode jti from new access
        val decoded = jwt.verifyAndDecode(tokens.accessToken)
        assertNotEquals(oldJti, decoded.jti)
        // ensure old jti was blocked
        verify(blocklist).block(eq(oldJti), any(), any())
    }

    @Test
    fun `expired refresh yields unauthorized`() {
        val sessionRepo: SessionJpaRepository = mock()
        val refreshRepo: RefreshTokenJpaRepository = mock()
        val blocklist: BlocklistCache = mock()

        val rid = "R2"
        val now = Instant.now()
        val refresh = RefreshTokenEntity(
            rid = rid,
            userId = 10,
            sessionJti = "J-X",
            issuedAt = now.minusSeconds(1000),
            expiresAt = now.minusSeconds(1),
            status = RefreshStatus.ACTIVE,
        )
        whenever(refreshRepo.findById(eq(rid))).thenReturn(Optional.of(refresh))

        val (svc, _) = newService(sessionRepo, refreshRepo, blocklist)
        assertThrows(UnauthorizeException::class.java) {
            svc.refresh(rid)
        }
    }
}
