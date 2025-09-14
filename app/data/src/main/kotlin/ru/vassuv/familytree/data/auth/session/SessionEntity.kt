package ru.vassuv.familytree.data.auth.session

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "sessions")
data class SessionEntity(
    @Id
    @Column(name = "jti", nullable = false, length = 64)
    val jti: String,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "active_family_id")
    val activeFamilyId: Long? = null,

    @Column(name = "scopes")
    val scopes: String? = null,

    @Column(name = "cap_version")
    val capVersion: Int = 1,

    @Column(name = "issued_at")
    val issuedAt: Instant = Instant.now(),

    @Column(name = "expires_at")
    val expiresAt: Instant,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    val status: SessionStatus = SessionStatus.ACTIVE,

    @Column(name = "meta")
    val meta: String? = null,
)

enum class SessionStatus { ACTIVE, REVOKED }

