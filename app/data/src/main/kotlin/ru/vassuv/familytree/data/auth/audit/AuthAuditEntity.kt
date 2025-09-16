package ru.vassuv.familytree.data.auth.audit

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "auth_audit")
data class AuthAuditEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long? = null,

    @Column(name = "event_time", nullable = false)
    val eventTime: Instant,

    @Column(name = "event_type", nullable = false, length = 64)
    val eventType: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false, length = 16)
    val result: AuditResult,

    @Column(name = "sid", length = 128)
    val sid: String? = null,

    @Column(name = "jti", length = 128)
    val jti: String? = null,

    @Column(name = "user_id")
    val userId: Long? = null,

    @Column(name = "details", columnDefinition = "TEXT")
    val details: String? = null,
)

enum class AuditResult { SUCCESS, FAILURE }
