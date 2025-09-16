package ru.vassuv.familytree.service.auth

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import ru.vassuv.familytree.data.auth.pending.PendingSessionRecord
import ru.vassuv.familytree.data.auth.pending.PendingSessionRepository
import ru.vassuv.familytree.data.auth.pending.PendingSessionStatus
import ru.vassuv.familytree.service.auth.audit.AuthAuditService
import ru.vassuv.familytree.service.auth.pending.SidGenerator

class TelegramServiceTest {

    @Test
    fun `createSession succeeds on first attempt`() {
        val repo: PendingSessionRepository = mock()
        val sidGen = object : SidGenerator() {
            override fun generate(byteLength: Int, prefix: String): String = "Sfixed1"
        }
        val tokenService: TokenService = mock()
        val audit: AuthAuditService = mock()
        val service = TelegramService(repo, sidGen, tokenService, audit)

        whenever(
            repo.create(
                eq("Sfixed1"),
                eq(PendingSessionRecord("Sfixed1", PendingSessionStatus.PENDING, invitationId = null)),
                eq(300L)
            )
        ).thenReturn(true)

        val created = service.createSession(null, 300)
        assertEquals("Sfixed1", created.sid)
        assertEquals(300L, created.expiresIn)
    }

    @Test
    fun `createSession retries once and then succeeds`() {
        val repo: PendingSessionRepository = mock()
        var counter = 0
        val sidGen = object : SidGenerator() {
            override fun generate(byteLength: Int, prefix: String): String = if (counter++ == 0) "Sfirst" else "Ssecond"
        }
        val tokenService: TokenService = mock()
        val audit: AuthAuditService = mock()
        val service = TelegramService(repo, sidGen, tokenService, audit)

        whenever(
            repo.create(
                eq("Sfirst"),
                eq(PendingSessionRecord("Sfirst", PendingSessionStatus.PENDING, invitationId = "inv-1")),
                eq(120L)
            )
        ).thenReturn(false)
        whenever(
            repo.create(
                eq("Ssecond"),
                eq(PendingSessionRecord("Ssecond", PendingSessionStatus.PENDING, invitationId = "inv-1")),
                eq(120L)
            )
        ).thenReturn(true)

        val created = service.createSession("inv-1", 120)
        assertEquals("Ssecond", created.sid)
        assertEquals(120L, created.expiresIn)
    }

    @Test
    fun `createSession fails after two attempts`() {
        val repo: PendingSessionRepository = mock()
        val tokenService: TokenService = mock()
        var counter = 0
        val sidGen = object : SidGenerator() {
            override fun generate(byteLength: Int, prefix: String): String = if (counter++ == 0) "Sa" else "Sb"
        }
        val audit: AuthAuditService = mock()
        val service = TelegramService(repo, sidGen, tokenService, audit)

        whenever(
            repo.create(
                eq("Sa"),
                any(),
                eq(60L)
            )
        ).thenReturn(false)
        whenever(
            repo.create(
                eq("Sb"),
                any(),
                eq(60L)
            )
        ).thenReturn(false)

        assertThrows(IllegalStateException::class.java) {
            service.createSession(null, 60)
        }
    }
}
