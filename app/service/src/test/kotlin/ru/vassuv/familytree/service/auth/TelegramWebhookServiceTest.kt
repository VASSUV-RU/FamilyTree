package ru.vassuv.familytree.service.auth

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import ru.vassuv.familytree.data.auth.pending.MarkReadyResult
import ru.vassuv.familytree.data.auth.pending.PendingSessionRecord
import ru.vassuv.familytree.data.auth.pending.PendingSessionRepository
import ru.vassuv.familytree.data.auth.pending.PendingSessionStatus

class TelegramWebhookServiceTest {

    private fun serviceWith(repo: PendingSessionRepository) = TelegramWebhookService(repo)

    private val sid = "Sabc"
    private val tg = TelegramUserInfo(id = 1, username = "u", firstName = "f", lastName = "l")

    @Test
    fun `returns not found when no session`() {
        val repo: PendingSessionRepository = mock()
        whenever(repo.get(sid)).thenReturn(null)
        val svc = serviceWith(repo)
        val res = svc.confirmStart(sid, tg)
        assertEquals(false, res.ok)
        assertEquals("Session not found or expired", res.message)
    }

    @Test
    fun `ok when markReady returns Ok`() {
        val repo: PendingSessionRepository = mock()
        whenever(repo.get(sid)).thenReturn(PendingSessionRecord(sid, PendingSessionStatus.PENDING))
        whenever(repo.markReady(eq(sid), any(), anyOrNull())).thenReturn(MarkReadyResult.Ok)
        val svc = serviceWith(repo)
        val res = svc.confirmStart(sid, tg)
        assertEquals(true, res.ok)
    }

    @Test
    fun `ok when AlreadyReady`() {
        val repo: PendingSessionRepository = mock()
        whenever(repo.get(sid)).thenReturn(PendingSessionRecord(sid, PendingSessionStatus.READY))
        whenever(repo.markReady(eq(sid), any(), anyOrNull())).thenReturn(MarkReadyResult.AlreadyReady)
        val svc = serviceWith(repo)
        val res = svc.confirmStart(sid, tg)
        assertEquals(true, res.ok)
    }

    @Test
    fun `false when AlreadyUsed`() {
        val repo: PendingSessionRepository = mock()
        whenever(repo.get(sid)).thenReturn(PendingSessionRecord(sid, PendingSessionStatus.USED))
        whenever(repo.markReady(eq(sid), any(), anyOrNull())).thenReturn(MarkReadyResult.AlreadyUsed)
        val svc = serviceWith(repo)
        val res = svc.confirmStart(sid, tg)
        assertEquals(false, res.ok)
        assertEquals("Session already used", res.message)
    }

    @Test
    fun `false when Conflict`() {
        val repo: PendingSessionRepository = mock()
        whenever(repo.get(sid)).thenReturn(PendingSessionRecord(sid, PendingSessionStatus.PENDING))
        whenever(repo.markReady(eq(sid), any(), anyOrNull())).thenReturn(MarkReadyResult.Conflict)
        val svc = serviceWith(repo)
        val res = svc.confirmStart(sid, tg)
        assertEquals(false, res.ok)
    }

    @Test
    fun `false when NotFound during markReady`() {
        val repo: PendingSessionRepository = mock()
        whenever(repo.get(sid)).thenReturn(PendingSessionRecord(sid, PendingSessionStatus.PENDING))
        whenever(repo.markReady(eq(sid), any(), anyOrNull())).thenReturn(MarkReadyResult.NotFound)
        val svc = serviceWith(repo)
        val res = svc.confirmStart(sid, tg)
        assertEquals(false, res.ok)
        assertEquals("Session not found or expired", res.message)
    }
}
