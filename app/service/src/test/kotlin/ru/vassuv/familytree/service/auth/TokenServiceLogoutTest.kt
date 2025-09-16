package ru.vassuv.familytree.service.auth

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import ru.vassuv.familytree.config.JwtProperties
import ru.vassuv.familytree.data.auth.session.BlocklistCache
import ru.vassuv.familytree.data.auth.session.SessionCache
import ru.vassuv.familytree.data.auth.session.SessionEntity
import ru.vassuv.familytree.data.auth.session.SessionJpaRepository
import ru.vassuv.familytree.data.auth.session.SessionStatus
import ru.vassuv.familytree.data.auth.session.RefreshTokenJpaRepository
import ru.vassuv.familytree.service.auth.audit.AuthAuditService
import java.time.Instant
import java.util.Optional

class TokenServiceLogoutTest {

    @Test
    fun `logout blocks jti and evicts cache`() {
        val sessionRepo: SessionJpaRepository = mock()
        val refreshRepo: RefreshTokenJpaRepository = mock()
        val sessionCache: SessionCache = mock()
        val blocklist: BlocklistCache = mock()
        val jwt = JwtService(JwtProperties())

        val jti = "J-1"
        val entity = SessionEntity(
            jti = jti,
            userId = 1,
            expiresAt = Instant.now().plusSeconds(120),
            status = SessionStatus.ACTIVE,
        )
        whenever(sessionRepo.findById(eq(jti))).thenReturn(Optional.of(entity))
        whenever(sessionRepo.save(any())).thenAnswer { it.arguments[0] }

        val invitation: ru.vassuv.familytree.service.invite.InvitationService = mock()
        val audit: AuthAuditService = mock()
        val svc = DefaultTokenService(sessionRepo, refreshRepo, sessionCache, blocklist, jwt, JwtProperties(), invitation, audit)
        svc.logout(jti)

        verify(blocklist).block(eq(jti), any(), any())
        verify(sessionCache).evict(eq(jti))
    }
}
