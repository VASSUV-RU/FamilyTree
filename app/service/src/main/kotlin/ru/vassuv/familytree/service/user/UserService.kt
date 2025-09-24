package ru.vassuv.familytree.service.user

import java.time.Instant

interface UserService {
    fun upsertFromTelegram(telegramId: Long, name: String?, avatarUrl: String?): User
}

data class User(
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val telegramId: Long?,
    val createdAt: Instant,
    val preferredFamilyId: String?,
)
