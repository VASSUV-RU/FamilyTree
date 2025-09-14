package ru.vassuv.familytree.data.auth.session

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "token_blocklist")
data class TokenBlockEntity(
    @Id
    @Column(name = "jti", nullable = false, length = 64)
    val jti: String,

    @Column(name = "reason")
    val reason: String? = null,

    @Column(name = "revoked_at")
    val revokedAt: Instant = Instant.now(),

    @Column(name = "expires_at")
    val expiresAt: Instant? = null,
)

