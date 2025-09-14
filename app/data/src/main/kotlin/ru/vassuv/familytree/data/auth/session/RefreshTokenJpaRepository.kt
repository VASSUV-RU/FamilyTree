package ru.vassuv.familytree.data.auth.session

import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenJpaRepository : JpaRepository<RefreshTokenEntity, String> {
    fun findAllBySessionJti(sessionJti: String): List<RefreshTokenEntity>
}

