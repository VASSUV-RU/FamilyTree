package ru.vassuv.familytree.service.auth

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.never
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import ru.vassuv.familytree.data.auth.pending.MarkReadyResult
import ru.vassuv.familytree.data.auth.pending.PendingSessionRecord
import ru.vassuv.familytree.data.auth.pending.PendingSessionRepository
import ru.vassuv.familytree.data.auth.pending.PendingSessionStatus
import ru.vassuv.familytree.service.auth.audit.AuthAuditService
import ru.vassuv.familytree.service.auth.pending.SidGenerator
import ru.vassuv.familytree.service.model.AuthTokens
import ru.vassuv.familytree.service.user.User
import ru.vassuv.familytree.service.user.UserService
import ru.vassuv.familytree.config.exception.ConflictException

class TelegramWebhookServiceTest {
    private val repo: PendingSessionRepository = mock()

    private val sidGen = object : SidGenerator() {
        override fun generate(byteLength: Int, prefix: String): String = "S"
    }

    private val tokenSvc = object : TokenService {
        override fun issueForTelegram(telegramId: Long, invitationId: String?) = AuthTokens("a","r")

        override fun refresh(refreshRid: String): AuthTokens {
            throw UnsupportedOperationException("Not used in this test")
        }

        override fun logout(jti: String) { /* no-op for test */ }

        override fun switchActiveFamily(userId: Long, currentJti: String, familyId: Long): AuthTokens {
            throw UnsupportedOperationException("Not used in this test")
        }
    }

    private val audit: AuthAuditService = mock()
    private val userService: UserService = mock()

    private val defaultUser = User(
        id = "user-1",
        name = "Test User",
        avatarUrl = null,
        telegramId = 1L,
        createdAt = java.time.Instant.now(),
        preferredFamilyId = null,
    )

    private val svc = TelegramService(repo, sidGen, tokenSvc, audit, userService)

    private val sid = "Sabc"
    private val tg = TelegramService.TelegramUserInfo(id = 1, username = "u", firstName = "f", lastName = "l")

    init {
        whenever(userService.upsertFromTelegram(any(), anyOrNull(), anyOrNull())).thenReturn(defaultUser)
    }

    @Test
    fun `returns not found when no session`() {
        whenever(repo.get(sid)).thenReturn(null)
        val res = svc.confirmStart(sid, tg)
        assertEquals(false, res.ok)
        assertEquals("Session not found or expired", res.message)
    }

    @Test
    fun `ok when markReady returns Ok`() {
        whenever(repo.get(sid)).thenReturn(PendingSessionRecord(sid, PendingSessionStatus.PENDING))
        whenever(repo.markReady(eq(sid), any(), any())).thenReturn(MarkReadyResult.Ok)
        val res = svc.confirmStart(sid, tg)
        assertEquals(true, res.ok)
    }

    @Test
    fun `ok when AlreadyReady`() {
        whenever(repo.get(sid)).thenReturn(PendingSessionRecord(sid, PendingSessionStatus.READY))
        whenever(repo.markReady(eq(sid), any(), any())).thenReturn(MarkReadyResult.AlreadyReady)
        val res = svc.confirmStart(sid, tg)
        assertEquals(true, res.ok)
    }

    @Test
    fun `false when AlreadyUsed`() {
        whenever(repo.get(sid)).thenReturn(PendingSessionRecord(sid, PendingSessionStatus.USED))
        whenever(repo.markReady(eq(sid), any(), any())).thenReturn(MarkReadyResult.AlreadyUsed)
        val res = svc.confirmStart(sid, tg)
        assertEquals(false, res.ok)
        assertEquals("Session already used", res.message)
    }

    @Test
    fun `false when Conflict`() {
        whenever(repo.get(sid)).thenReturn(PendingSessionRecord(sid, PendingSessionStatus.PENDING))
        whenever(repo.markReady(eq(sid), any(), any())).thenReturn(MarkReadyResult.Conflict)
        val res = svc.confirmStart(sid, tg)
        assertEquals(false, res.ok)
    }

    @Test
    fun `false when NotFound during markReady`() {
        whenever(repo.get(sid)).thenReturn(PendingSessionRecord(sid, PendingSessionStatus.PENDING))
        whenever(repo.markReady(eq(sid), any(), any())).thenReturn(MarkReadyResult.NotFound)
        val res = svc.confirmStart(sid, tg)
        assertEquals(false, res.ok)
        assertEquals("Session not found or expired", res.message)
    }

    @Test
    fun `false when user creation conflicts`() {
        whenever(repo.get(sid)).thenReturn(PendingSessionRecord(sid, PendingSessionStatus.PENDING))
        whenever(userService.upsertFromTelegram(any(), any(), anyOrNull())).thenThrow(ConflictException("dup"))

        val res = svc.confirmStart(sid, tg)

        assertEquals(false, res.ok)
        assertEquals("Telegram account already linked", res.message)
        org.mockito.kotlin.verify(repo, never()).markReady(any(), any(), any())
    }
}
