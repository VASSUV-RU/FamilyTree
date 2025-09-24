package ru.vassuv.familytree.data.user

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import java.time.Instant

@DataJpaTest(properties = ["spring.liquibase.enabled=false"])
@EntityScan(basePackageClasses = [UserEntity::class])
@EnableJpaRepositories(basePackageClasses = [UserJpaRepository::class])
class UserJpaRepositoryTest @Autowired constructor(
    private val repository: UserJpaRepository,
) {

    @Test
    fun `save and find by telegramId`() {
        val entity = UserEntity(
            id = "user-1",
            name = "Alice",
            telegramId = 1001L,
            createdAt = Instant.parse("2025-09-22T10:15:30Z"),
        )
        repository.saveAndFlush(entity)

        val loaded = repository.findByTelegramId(1001L)
        assertNotNull(loaded)
        assertEquals("user-1", loaded!!.id)
        assertEquals("Alice", loaded.name)
    }

    @Test
    fun `unique telegram id constraint`() {
        repository.saveAndFlush(
            UserEntity(
                id = "user-a",
                name = "First",
                telegramId = 9999L,
                createdAt = Instant.now(),
            )
        )

        val duplicate = UserEntity(
            id = "user-b",
            name = "Duplicate",
            telegramId = 9999L,
            createdAt = Instant.now(),
        )

        assertThrows(DataIntegrityViolationException::class.java) {
            repository.saveAndFlush(duplicate)
        }
    }
}
