package ru.vassuv.familytree.data.auth.pending

import java.time.Instant

enum class PendingSessionStatus { PENDING, READY, USED }

data class PendingSessionRecord(
    val sid: String,
    val status: PendingSessionStatus,
    val createdAt: Long = Instant.now().epochSecond,
    val invitationId: String? = null,
    val userId: String? = null,
    val auth: Map<String, Any?>? = null,
)

sealed interface MarkReadyResult {
    object Ok : MarkReadyResult
    object NotFound : MarkReadyResult
    object AlreadyReady : MarkReadyResult
    object AlreadyUsed : MarkReadyResult
    object Conflict : MarkReadyResult
}

sealed interface MarkUsedResult {
    object Ok : MarkUsedResult
    object NotFound : MarkUsedResult
    object Conflict : MarkUsedResult
}

interface PendingSessionRepository {
    fun create(sid: String, record: PendingSessionRecord, ttlSeconds: Long): Boolean
    fun get(sid: String): PendingSessionRecord?
    fun markReady(sid: String, authPayload: Map<String, Any?>, userId: String?): MarkReadyResult
    fun markUsed(sid: String): MarkUsedResult
    fun isKnown(sid: String): Boolean
}