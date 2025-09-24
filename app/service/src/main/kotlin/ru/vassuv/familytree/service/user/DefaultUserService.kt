package ru.vassuv.familytree.service.user

import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import ru.vassuv.familytree.config.exception.conflictError
import ru.vassuv.familytree.data.user.UserEntity
import ru.vassuv.familytree.data.user.UserJpaRepository
import java.time.Clock
import java.time.Instant
import java.util.UUID

@Service
class DefaultUserService(
    private val repository: UserJpaRepository,
    private val clock: Clock = Clock.systemUTC(),
) : UserService {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun upsertFromTelegram(telegramId: Long, name: String?, avatarUrl: String?): User {
        val sanitizedName = sanitizeName(name, telegramId)
        val sanitizedAvatar = avatarUrl?.takeUnless { it.isBlank() }

        val existing = repository.findByTelegramId(telegramId)
        if (existing != null) {
            val updated = updateExisting(existing, sanitizedName, sanitizedAvatar, telegramId)
            return updated.toModel()
        }

        val entity = UserEntity(
            id = UUID.randomUUID().toString(),
            name = sanitizedName,
            avatarUrl = sanitizedAvatar,
            telegramId = telegramId,
            createdAt = Instant.now(clock),
        )
        return saveCatching(entity).toModel()
    }

    private fun updateExisting(entity: UserEntity, name: String, avatarUrl: String?, telegramId: Long): UserEntity {
        var changed = false
        if (entity.name != name) {
            entity.name = name
            changed = true
        }
        if (entity.avatarUrl != avatarUrl) {
            entity.avatarUrl = avatarUrl
            changed = true
        }
        if (entity.telegramId != telegramId) {
            entity.telegramId = telegramId
            changed = true
        }
        return if (changed) saveCatching(entity) else entity
    }

    private fun saveCatching(entity: UserEntity): UserEntity {
        return runCatching { repository.saveAndFlush(entity) }
            .onFailure { ex ->
                if (ex is DataIntegrityViolationException) {
                    logger.warn("User upsert conflict for telegramId={}", entity.telegramId)
                    conflictError("Telegram account already linked to another user")
                }
            }
            .getOrThrow()
    }

    private fun sanitizeName(name: String?, telegramId: Long): String {
        val fromName = name?.trim()?.takeIf { it.isNotEmpty() }
        return fromName ?: "Telegram user $telegramId"
    }

    private fun UserEntity.toModel(): User = User(
        id = id,
        name = name,
        avatarUrl = avatarUrl,
        telegramId = telegramId,
        createdAt = createdAt,
        preferredFamilyId = preferredFamilyId,
    )
}
