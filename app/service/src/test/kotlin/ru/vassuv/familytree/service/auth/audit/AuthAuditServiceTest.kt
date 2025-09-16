package ru.vassuv.familytree.service.auth.audit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import ru.vassuv.familytree.data.auth.audit.AuditResult
import ru.vassuv.familytree.data.auth.audit.AuthAuditRecord
import ru.vassuv.familytree.data.auth.audit.AuthAuditRepository

class AuthAuditServiceTest {

    private val repository: AuthAuditRepository = mock()
    private val service = AuthAuditService(repository)

    @Test
    fun `logSuccess delegates to repository`() {
        val recordCaptor = argumentCaptor<AuthAuditRecord>()

        service.logSuccess(
            event = AuthAuditEvent.TOKEN_ISSUED,
            sid = "S123",
            jti = "J123",
            userId = 99,
            details = mapOf("key" to "value")
        )

        verify(repository).save(recordCaptor.capture())
        val saved = recordCaptor.firstValue
        assertEquals(AuthAuditEvent.TOKEN_ISSUED.code, saved.eventType)
        assertEquals(AuditResult.SUCCESS, saved.result)
        assertEquals("S123", saved.sid)
        assertEquals("J123", saved.jti)
        assertEquals(99, saved.userId)
        assertEquals(mapOf("key" to "value"), saved.details)
    }

    @Test
    fun `logFailure enriches details with reason`() {
        val recordCaptor = argumentCaptor<AuthAuditRecord>()

        service.logFailure(
            event = AuthAuditEvent.TOKEN_REFRESH_FAILED,
            sid = "Sfail",
            jti = "Jfail",
            userId = 1,
            reason = "oops",
            details = mapOf("rid" to "R1")
        )

        verify(repository).save(recordCaptor.capture())
        val saved = recordCaptor.firstValue
        assertEquals(AuditResult.FAILURE, saved.result)
        assertEquals("oops", saved.details?.get("reason"))
        assertEquals("R1", saved.details?.get("rid"))
    }
}
