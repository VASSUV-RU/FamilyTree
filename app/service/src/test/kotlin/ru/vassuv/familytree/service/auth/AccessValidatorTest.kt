package ru.vassuv.familytree.service.auth

import org.junit.jupiter.api.Assertions.assertEquals
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
import ru.vassuv.familytree.data.auth.session.CachedSession
import ru.vassuv.familytree.data.auth.session.SessionEntity
import ru.vassuv.familytree.data.auth.session.SessionJpaRepository
import ru.vassuv.familytree.data.auth.session.SessionStatus
import ru.vassuv.familytree.data.auth.session.SessionCache
import java.time.Instant
import java.util.*

class AccessValidatorTest {

    private fun newJwtService() = JwtService(JwtProperties())

    @Test
    fun `valid token and cached session passes`() {
        val jwt = newJwtService()
        val token = jwt.sign(subject = "42", jti = "J1", expiresAt = Instant.now().plusSeconds(60))

        val cache: SessionCache = mock()
        val repo: SessionJpaRepository = mock()
        val blk: BlocklistCache = mock()

        whenever(blk.isBlocked("J1")).thenReturn(false)
        whenever(cache.get("J1")).thenReturn(CachedSession(jti = "J1", userId = 42))

        val validator = AccessValidator(jwt, cache, repo, blk)
        val vs = validator.validate(token)
        assertEquals(42, vs.userId)
        assertEquals("J1", vs.jti)
        verify(cache).get("J1")
    }

    @Test
    fun `valid token loads from repo when cache miss`() {
        val jwt = newJwtService()
        val token = jwt.sign(subject = "100", jti = "J2", expiresAt = Instant.now().plusSeconds(60))
        val cache: SessionCache = mock()
        val repo: SessionJpaRepository = mock()
        val blk: BlocklistCache = mock()

        whenever(blk.isBlocked("J2")).thenReturn(false)
        whenever(cache.get("J2")).thenReturn(null)
        whenever(repo.findById(eq("J2"))).thenReturn(Optional.of(
            SessionEntity(
                jti = "J2",
                userId = 100,
                expiresAt = Instant.now().plusSeconds(60),
                status = SessionStatus.ACTIVE,
            )
        ))

        val validator = AccessValidator(jwt, cache, repo, blk)
        val vs = validator.validate(token)
        assertEquals(100, vs.userId)
    }

    @Test
    fun `blocked jti is unauthorized`() {
        val jwt = newJwtService()
        val token = jwt.sign(subject = "100", jti = "J3", expiresAt = Instant.now().plusSeconds(60))
        val cache: SessionCache = mock()
        val repo: SessionJpaRepository = mock()
        val blk: BlocklistCache = mock()

        whenever(blk.isBlocked("J3")).thenReturn(true)
        val validator = AccessValidator(jwt, cache, repo, blk)
        assertThrows(UnauthorizeException::class.java) {
            validator.validate(token)
        }
    }

    @Test
    fun `expired token is unauthorized`() {
        val jwt = newJwtService()
        val token = jwt.sign(subject = "100", jti = "J4", expiresAt = Instant.now().minusSeconds(1))
        val cache: SessionCache = mock()
        val repo: SessionJpaRepository = mock()
        val blk: BlocklistCache = mock()
        val validator = AccessValidator(jwt, cache, repo, blk)
        assertThrows(UnauthorizeException::class.java) {
            validator.validate(token)
        }
    }
}

