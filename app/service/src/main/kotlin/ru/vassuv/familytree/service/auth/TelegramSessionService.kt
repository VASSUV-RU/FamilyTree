package ru.vassuv.familytree.service.auth

import org.springframework.stereotype.Service
import ru.vassuv.familytree.data.auth.pending.PendingSessionRecord
import ru.vassuv.familytree.data.auth.pending.PendingSessionRepository
import ru.vassuv.familytree.data.auth.pending.PendingSessionStatus.pending
import ru.vassuv.familytree.service.auth.pending.SidGenerator

data class CreatedTelegramSession(
    val sid: String,
    val expiresIn: Long,
)

@Service
class TelegramSessionService(
    private val pendingRepo: PendingSessionRepository,
    private val sidGenerator: SidGenerator,
) {
    fun createSession(invitationId: String?, ttlSeconds: Long): CreatedTelegramSession {
        fun String.mapToSession() = CreatedTelegramSession(this, ttlSeconds)
        fun sid(): String? {
            val sid = sidGenerator.generate()
            val record = PendingSessionRecord(sid = sid, status = pending, invitationId = invitationId)
            val ok = pendingRepo.create(sid, record, ttlSeconds)
            return if(ok) sid else null
        }
        val sid = sid() ?: sid() ?: error("Failed to create pending session")
        return sid.mapToSession()
    }
}

