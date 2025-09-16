package ru.vassuv.familytree.service.auth

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import ru.vassuv.familytree.config.JwtProperties
import ru.vassuv.familytree.config.exception.UnauthorizeException
import ru.vassuv.familytree.data.auth.session.BlocklistCache
import ru.vassuv.familytree.data.auth.session.RefreshStatus
import ru.vassuv.familytree.data.auth.session.RefreshTokenEntity
import ru.vassuv.familytree.data.auth.session.RefreshTokenJpaRepository
import ru.vassuv.familytree.data.auth.session.SessionCache
import ru.vassuv.familytree.data.auth.session.SessionEntity
import ru.vassuv.familytree.data.auth.session.SessionJpaRepository
import ru.vassuv.familytree.data.auth.session.SessionStatus
import java.time.Instant
import java.util.Optional

class TokenServiceSwitchFamilyTest {

    @Test
    fun `switchActiveFamily updates activeFamilyId and returns new access using same jti`() {
        val sessionRepo: SessionJpaRepository = mock()
        val refreshRepo: RefreshTokenJpaRepository = mock()
        val sessionCache: SessionCache = mock()
        val blocklist: BlocklistCache = mock()
        val jwt = JwtService(JwtProperties())

        val jti = "J-100"
        val userId = 10L
        val now = Instant.now()
        val existing = SessionEntity(
            jti = jti,
            userId = userId,
            activeFamilyId = null,
            scopes = "s1",
            capVersion = 1,
            issuedAt = now.minusSeconds(60),
            expiresAt = now.plusSeconds(300),
            status = SessionStatus.ACTIVE,
        )
        whenever(sessionRepo.findById(eq(jti))).thenReturn(Optional.of(existing))
        whenever(sessionRepo.save(any())).thenAnswer { it.arguments[0] }

        // existing active refresh bound to same jti
        whenever(refreshRepo.findAllBySessionJti(eq(jti))).thenReturn(
            listOf(
                RefreshTokenEntity(
                    rid = "R-keep",
                    userId = userId,
                    sessionJti = jti,
                    expiresAt = now.plusSeconds(3600),
                    status = RefreshStatus.ACTIVE,
                )
            )
        )

        val invitation: ru.vassuv.familytree.service.invite.InvitationService = mock()
        val svc = DefaultTokenService(sessionRepo, refreshRepo, sessionCache, blocklist, jwt, JwtProperties(), invitation)
        val tokens = svc.switchActiveFamily(userId = userId, currentJti = jti, familyId = 77)

        // verify session saved with updated family and capVersion incremented
        verify(sessionRepo).save(
            argThat { this is SessionEntity && this.jti == jti && this.activeFamilyId == 77L && this.capVersion == 2 }
        )
        // verify cache updated accordingly
        verify(sessionCache).put(
            argThat { this.jti == jti && this.userId == userId && this.activeFamilyId == 77L && this.capVersion == 2 },
            any()
        )
        // refresh reused
        assertEquals("R-keep", tokens.refreshToken)
        // access uses same jti
        val decoded = jwt.verifyAndDecode(tokens.accessToken)
        assertEquals(jti, decoded.jti)

        // no creation of a new refresh when existing one is active
        verify(refreshRepo, never()).save(any())
    }

    @Test
    fun `switchActiveFamily fails when session belongs to another user`() {
        val sessionRepo: SessionJpaRepository = mock()
        val refreshRepo: RefreshTokenJpaRepository = mock()
        val sessionCache: SessionCache = mock()
        val blocklist: BlocklistCache = mock()
        val jwt = JwtService(JwtProperties())

        val jti = "J-x"
        val existing = SessionEntity(
            jti = jti,
            userId = 999,
            expiresAt = Instant.now().plusSeconds(300),
            status = SessionStatus.ACTIVE,
        )
        whenever(sessionRepo.findById(eq(jti))).thenReturn(Optional.of(existing))

        val invitation: ru.vassuv.familytree.service.invite.InvitationService = mock()
        val svc = DefaultTokenService(sessionRepo, refreshRepo, sessionCache, blocklist, jwt, JwtProperties(), invitation)
        assertThrows(UnauthorizeException::class.java) {
            svc.switchActiveFamily(userId = 1, currentJti = jti, familyId = 55)
        }
    }

    @Test
    fun `switchActiveFamily rejects non-positive familyId`() {
        val sessionRepo: SessionJpaRepository = mock()
        val refreshRepo: RefreshTokenJpaRepository = mock()
        val sessionCache: SessionCache = mock()
        val blocklist: BlocklistCache = mock()
        val jwt = JwtService(JwtProperties())

        val invitation: ru.vassuv.familytree.service.invite.InvitationService = mock()
        val svc = DefaultTokenService(sessionRepo, refreshRepo, sessionCache, blocklist, jwt, JwtProperties(), invitation)
        assertThrows(IllegalArgumentException::class.java) {
            svc.switchActiveFamily(userId = 1, currentJti = "J", familyId = 0)
        }
    }
}
