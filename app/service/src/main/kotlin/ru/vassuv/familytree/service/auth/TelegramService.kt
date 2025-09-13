package ru.vassuv.familytree.service.auth

import org.springframework.stereotype.Service
import ru.vassuv.familytree.config.exception.conflictError
import ru.vassuv.familytree.config.exception.goneError
import ru.vassuv.familytree.config.exception.notFoundError
import kotlin.math.min
import ru.vassuv.familytree.data.auth.pending.MarkUsedResult
import ru.vassuv.familytree.data.auth.pending.AuthPayload
import ru.vassuv.familytree.data.auth.pending.PendingSessionRecord
import ru.vassuv.familytree.data.auth.pending.PendingSessionRepository
import ru.vassuv.familytree.data.auth.pending.PendingSessionStatus.PENDING
import ru.vassuv.familytree.data.auth.pending.PendingSessionStatus.READY
import ru.vassuv.familytree.data.auth.pending.PendingSessionStatus.USED
import ru.vassuv.familytree.service.auth.pending.SidGenerator
import ru.vassuv.familytree.service.model.CreatedTelegramSession

sealed interface PollDelivery {
  object Pending : PollDelivery
  data class Ready(val auth: AuthPayload) : PollDelivery
}

@Service
class TelegramService(
  private val pendingRepo: PendingSessionRepository,
  private val sidGenerator: SidGenerator,
) {
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
          val auth = session.auth ?: conflictError("Missing auth payload")
          markUsed(sid)
          return PollDelivery.Ready(auth = auth)
        }
      }
    }
  }


  fun markUsed(sid: String): MarkUsedResult = pendingRepo.markUsed(sid)

}
