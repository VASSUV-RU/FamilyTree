package ru.vassuv.familytree.service.auth

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import ru.vassuv.familytree.config.JwtProperties
import java.time.Instant

class JwtServiceTest {

    @Test
    fun `sign and verify returns same sub and jti`() {
        val svc = JwtService(JwtProperties())
        val now = Instant.now()
        val jwt = svc.sign(subject = "123", jti = "j-1", expiresAt = now.plusSeconds(60))
        val v = svc.verifyAndDecode(jwt)
        assertEquals("123", v.subject)
        assertEquals("j-1", v.jti)
    }

    @Test
    fun `expired token is rejected`() {
        val svc = JwtService(JwtProperties())
        val jwt = svc.sign(subject = "123", jti = "j-2", expiresAt = Instant.now().minusSeconds(10))
        assertThrows(SecurityException::class.java) {
            svc.verifyAndDecode(jwt)
        }
    }
}

