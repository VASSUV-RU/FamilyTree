package ru.vassuv.familytree.service.auth.audit

import org.springframework.stereotype.Service
import ru.vassuv.familytree.data.auth.audit.AuditResult
import ru.vassuv.familytree.data.auth.audit.AuthAuditRecord
import ru.vassuv.familytree.data.auth.audit.AuthAuditRepository

enum class AuthAuditEvent(val code: String) {
    TELEGRAM_SESSION_CREATED("telegram.session.created"),
    TELEGRAM_SESSION_CONFIRMED("telegram.session.confirmed"),
    TELEGRAM_SESSION_CONFIRM_FAILED("telegram.session.confirm.failed"),
    TOKEN_ISSUED("token.issued"),
    TOKEN_REFRESHED("token.refreshed"),
    TOKEN_REFRESH_FAILED("token.refresh.failed"),
    SESSION_LOGOUT("session.logout"),
    ACTIVE_FAMILY_SWITCHED("session.active-family.switched"),
}

@Service
class AuthAuditService(
    private val repository: AuthAuditRepository,
) {

    fun logSuccess(
        event: AuthAuditEvent,
        sid: String? = null,
        jti: String? = null,
        userId: Long? = null,
        details: Map<String, Any?>? = null,
    ) {
        repository.save(
            AuthAuditRecord(
                eventType = event.code,
                result = AuditResult.SUCCESS,
                sid = sid,
                jti = jti,
                userId = userId,
                details = details,
            )
        )
    }

    fun logFailure(
        event: AuthAuditEvent,
        sid: String? = null,
        jti: String? = null,
        userId: Long? = null,
        reason: String? = null,
        details: Map<String, Any?>? = null,
    ) {
        val extra = when {
            reason != null && !reason.isBlank() -> (details ?: emptyMap()) + mapOf("reason" to reason)
            else -> details
        }
        repository.save(
            AuthAuditRecord(
                eventType = event.code,
                result = AuditResult.FAILURE,
                sid = sid,
                jti = jti,
                userId = userId,
                details = extra,
            )
        )
    }
}
