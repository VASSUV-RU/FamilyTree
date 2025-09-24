package ru.vassuv.familytree.data.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    @Column(name = "id", length = 36, nullable = false)
    val id: String = "",

    @Column(name = "name", length = 128, nullable = false)
    var name: String = "",

    @Column(name = "avatar_url", length = 512)
    var avatarUrl: String? = null,

    @Column(name = "telegram_id", unique = true)
    var telegramId: Long? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.EPOCH,

    @Column(name = "preferred_family_id", length = 36)
    var preferredFamilyId: String? = null,
)
