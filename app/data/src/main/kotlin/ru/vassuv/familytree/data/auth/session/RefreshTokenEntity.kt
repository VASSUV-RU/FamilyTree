package ru.vassuv.familytree.data.auth.session

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "refresh_tokens")
data class RefreshTokenEntity(
    @Id
    @Column(name = "rid", nullable = false, length = 64)
    val rid: String,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "session_jti", nullable = false, length = 64)
    val sessionJti: String,

    @Column(name = "issued_at")
    val issuedAt: Instant = Instant.now(),

    @Column(name = "expires_at")
    val expiresAt: Instant,

    @Column(name = "revoked_at")
    val revokedAt: Instant? = null,

    @Column(name = "rotated_at")
    val rotatedAt: Instant? = null,

    @Column(name = "fingerprint_hash")
    val fingerprintHash: String? = null,

    @Column(name = "device")
    val device: String? = null,

    @Column(name = "ip")
    val ip: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    val status: RefreshStatus = RefreshStatus.ACTIVE,
)

enum class RefreshStatus { ACTIVE, REVOKED }

