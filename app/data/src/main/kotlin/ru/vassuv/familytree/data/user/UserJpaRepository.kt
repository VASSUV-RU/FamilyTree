package ru.vassuv.familytree.data.user

import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository : JpaRepository<UserEntity, String> {
    fun findByTelegramId(telegramId: Long): UserEntity?
}
