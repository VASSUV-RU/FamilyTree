package ru.vassuv.familytree.service.auth

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import ru.vassuv.familytree.config.JwtProperties
import ru.vassuv.familytree.data.auth.session.RefreshTokenJpaRepository
import ru.vassuv.familytree.data.auth.session.SessionCache
import ru.vassuv.familytree.data.auth.session.SessionEntity
import ru.vassuv.familytree.data.auth.session.SessionJpaRepository
import ru.vassuv.familytree.data.auth.session.BlocklistCache
import ru.vassuv.familytree.service.auth.audit.AuthAuditService
import ru.vassuv.familytree.service.invite.InvitationService
import java.time.Instant

class TokenServiceIssueInviteTest {

    @Test
    fun `issueForTelegram accepts invitation and sets activeFamily`() {
        val sessionRepo: SessionJpaRepository = mock()
        val refreshRepo: RefreshTokenJpaRepository = mock()
        val sessionCache: SessionCache = mock()
        val blocklist: BlocklistCache = mock()
        val invitation: InvitationService = mock()
        val jwt = JwtService(JwtProperties())

        whenever(invitation.accept(eq("inv-1"), eq(777L))).thenReturn(555L)
        whenever(sessionRepo.save(any())).thenAnswer { it.arguments[0] }
        whenever(refreshRepo.save(any())).thenAnswer { it.arguments[0] }

        val audit: AuthAuditService = mock()
        val svc = DefaultTokenService(sessionRepo, refreshRepo, sessionCache, blocklist, jwt, JwtProperties(), invitation, audit)
        val tokens = svc.issueForTelegram(telegramId = 777L, invitationId = "inv-1")

        // verify cache was set with activeFamilyId 555
        verify(sessionCache).put(
            org.mockito.kotlin.argThat { this.activeFamilyId == 555L },
            org.mockito.kotlin.any()
        )
        // decode and ensure jti present
        val decoded = jwt.verifyAndDecode(tokens.accessToken)
        assertEquals("777", decoded.subject)
    }
}
