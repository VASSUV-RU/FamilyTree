package ru.vassuv.familytree.service.auth

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.vassuv.familytree.config.exception.UnauthorizeException
import ru.vassuv.familytree.data.auth.pending.MarkReadyResult
import ru.vassuv.familytree.data.auth.pending.AuthPayload
import ru.vassuv.familytree.data.auth.pending.PendingSessionRecord
import ru.vassuv.familytree.data.auth.pending.PendingSessionRepository
import java.time.Instant
import java.util.UUID

data class TelegramUserInfo(
    val id: Long,
    val username: String?,
    val firstName: String?,
    val lastName: String?,
)

data class WebhookConfirmResult(
    val ok: Boolean,
    val message: String,
)

@Service
class TelegramWebhookService(
    private val pendingRepo: PendingSessionRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun confirmStart(sid: String, tg: TelegramUserInfo): WebhookConfirmResult {
        val existing: PendingSessionRecord = pendingRepo.get(sid)
            ?: return WebhookConfirmResult(false, "Session not found or expired")

        // Prepare minimal auth payload; tokens and invites will be added in later tasks
        val userId = existing.userId ?: "u:tg:${tg.id}"
        val jti = UUID.randomUUID().toString()
        val access = "access-" + jti
        val payload = AuthPayload(
            jti = jti,
            accessToken = access,
            refreshToken = jti,
            userId = userId,
            telegramId = tg.id,
            username = tg.username,
            firstName = tg.firstName,
            lastName = tg.lastName,
            issuedAt = Instant.now().epochSecond,
        )

        return when (val res = pendingRepo.markReady(sid, payload, userId)) {
            is MarkReadyResult.Ok -> WebhookConfirmResult(true, "Session confirmed. You can return to app.")
            is MarkReadyResult.AlreadyReady -> WebhookConfirmResult(true, "Already confirmed. You can return to app.")
            is MarkReadyResult.AlreadyUsed -> WebhookConfirmResult(false, "Session already used")
            is MarkReadyResult.NotFound -> WebhookConfirmResult(false, "Session not found or expired")
            is MarkReadyResult.Conflict -> {
                logger.warn("markReady conflict for sid={}", sid)
                WebhookConfirmResult(false, "Temporary conflict, try again")
            }
        }
    }

    fun parseStartSid(text: String): String? {
        val t = text.trim()
        if (!t.startsWith("/start")) return null
        val parts = t.split(Regex("\\s+"))
        return if (parts.size >= 2) parts[1] else null
    }

    fun validateSecretOrThrow(provided: String?, expected: String) {
        if (expected.isBlank() || provided == null || provided != expected) {
            throw UnauthorizeException()
        }
    }
}
