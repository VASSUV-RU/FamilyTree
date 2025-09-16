package ru.vassuv.familytree.data.auth.audit

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.time.Instant

interface AuthAuditRepository {
    fun save(record: AuthAuditRecord)
}

data class AuthAuditRecord(
    val eventType: String,
    val result: AuditResult,
    val sid: String? = null,
    val jti: String? = null,
    val userId: Long? = null,
    val details: Map<String, Any?>? = null,
    val eventTime: Instant = Instant.now(),
)

@Repository
class AuthAuditRepositoryJpa(
    private val jpa: AuthAuditJpaRepository,
    private val objectMapper: ObjectMapper,
) : AuthAuditRepository {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun save(record: AuthAuditRecord) {
        val detailsJson = record.details?.takeIf { it.isNotEmpty() }?.let {
            runCatching { objectMapper.writeValueAsString(it) }
                .onFailure { err -> logger.warn("Failed to serialize audit details: {}", err.message) }
                .getOrNull()
        }
        val entity = AuthAuditEntity(
            eventTime = record.eventTime,
            eventType = record.eventType,
            result = record.result,
            sid = record.sid,
            jti = record.jti,
            userId = record.userId,
            details = detailsJson,
        )
        jpa.save(entity)
    }
}
