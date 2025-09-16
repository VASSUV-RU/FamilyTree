package ru.vassuv.familytree.data.auth.audit

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import org.mockito.ArgumentCaptor.captor
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class AuthAuditRepositoryJpaTest {

    private val jpa: AuthAuditJpaRepository = mock()
    private val repository = AuthAuditRepositoryJpa(jpa, jacksonObjectMapper())

    @Test
    fun `save converts record to entity with json details`() {
        val capture = captor<AuthAuditEntity>()
        val record = AuthAuditRecord(
            eventType = "token.issued",
            result = AuditResult.SUCCESS,
            sid = "S1",
            jti = "J1",
            userId = 42,
            details = mapOf("a" to 1, "b" to "text"),
            eventTime = Instant.parse("2025-01-01T00:00:00Z"),
        )

        repository.save(record)

        verify(jpa).save(capture.capture())
        val entity = capture.value
        assertEquals("token.issued", entity.eventType)
        assertEquals(AuditResult.SUCCESS, entity.result)
        assertEquals("S1", entity.sid)
        assertEquals("J1", entity.jti)
        assertEquals(42, entity.userId)
        assertEquals("2025-01-01T00:00:00Z", entity.eventTime.toString())
        // Details serialized as JSON string containing keys
        val json = entity.details ?: throw AssertionError("details null")
        assertTrue(json.contains("\"a\":1"))
        assertTrue(json.contains("\"b\":\"text\""))
    }
}
