package ru.vassuv.familytree.service.auth

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.vassuv.familytree.config.exception.UnauthorizeException
import ru.vassuv.familytree.config.exception.conflictError
import ru.vassuv.familytree.config.exception.goneError
import ru.vassuv.familytree.config.exception.notFoundError
import kotlin.math.min
import ru.vassuv.familytree.data.auth.pending.MarkUsedResult
import ru.vassuv.familytree.service.model.AuthTokens
import ru.vassuv.familytree.data.auth.pending.PendingSessionRecord
import ru.vassuv.familytree.data.auth.pending.PendingSessionRepository
import ru.vassuv.familytree.data.auth.pending.PendingSessionStatus.PENDING
import ru.vassuv.familytree.data.auth.pending.PendingSessionStatus.READY
import ru.vassuv.familytree.data.auth.pending.PendingSessionStatus.USED
import ru.vassuv.familytree.service.auth.pending.SidGenerator
import ru.vassuv.familytree.service.model.CreatedTelegramSession

sealed interface PollDelivery {
  object Pending : PollDelivery
  data class Ready(val tokens: AuthTokens) : PollDelivery
}

@Service
class TelegramService(
  private val pendingRepo: PendingSessionRepository,
  private val sidGenerator: SidGenerator,
  private val tokenService: TokenService,
) {
  private val logger = LoggerFactory.getLogger(javaClass)
  fun createSession(invitationId: String?, ttlSeconds: Long): CreatedTelegramSession {
    fun generateSid(): String? {
      val sid = sidGenerator.generate()
      val record = PendingSessionRecord(sid = sid, status = PENDING, invitationId = invitationId)
      val ok = pendingRepo.create(sid, record, ttlSeconds)
      return if (ok) sid else null
    }

    val sid = generateSid() ?: generateSid() ?: error("Failed to create pending session")
    return CreatedTelegramSession(sid, ttlSeconds)
  }

  fun awaitLoginSession(sid: String, waitSeconds: Long = 25L): PollDelivery {
    fun now() = System.currentTimeMillis()
    val start = now()
    val deadline = start + waitSeconds * 1000
    while (true) {
      val session = pendingRepo.get(sid)
      when(session?.status) {
        null -> when {
          pendingRepo.isKnown(sid) -> goneError("Session is expired")
          else -> notFoundError("Session not found")
        }
        PENDING -> when {
          now() >= deadline -> return PollDelivery.Pending
          else -> runCatching {
            Thread.sleep( min(300, (deadline - now()).coerceAtLeast(0)))
          }
        }
        USED -> conflictError("Session is already used")
        READY -> {
          val tgId = session.telegramId ?: conflictError("Missing telegramId")
          val tokens = tokenService.issueForTelegram(tgId, session.invitationId)
          markUsed(sid)
          return PollDelivery.Ready(tokens = tokens)
        }
      }
    }
  }


  fun markUsed(sid: String): MarkUsedResult = pendingRepo.markUsed(sid)

  // --- Webhook helpers and confirm moved here ---
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

  fun confirmStart(sid: String, tg: TelegramUserInfo): WebhookConfirmResult {
    val existing: PendingSessionRecord = pendingRepo.get(sid)
      ?: return WebhookConfirmResult(false, "Session not found or expired")

    return when (val res = pendingRepo.markReady(sid, tg.id)) {
      is ru.vassuv.familytree.data.auth.pending.MarkReadyResult.Ok -> WebhookConfirmResult(true, "Session confirmed. You can return to app.")
      is ru.vassuv.familytree.data.auth.pending.MarkReadyResult.AlreadyReady -> WebhookConfirmResult(true, "Already confirmed. You can return to app.")
      is ru.vassuv.familytree.data.auth.pending.MarkReadyResult.AlreadyUsed -> WebhookConfirmResult(false, "Session already used")
      is ru.vassuv.familytree.data.auth.pending.MarkReadyResult.NotFound -> WebhookConfirmResult(false, "Session not found or expired")
      is ru.vassuv.familytree.data.auth.pending.MarkReadyResult.Conflict -> {
        logger.warn("markReady conflict for sid={}", sid)
        WebhookConfirmResult(false, "Temporary conflict, try again")
      }
    }
  }

}
