package ru.vassuv.familytree.service.user

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.dao.DataIntegrityViolationException
import ru.vassuv.familytree.config.exception.ConflictException
import ru.vassuv.familytree.data.user.UserEntity
import ru.vassuv.familytree.data.user.UserJpaRepository
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class DefaultUserServiceTest {

    private val clock: Clock = Clock.fixed(Instant.parse("2025-09-22T12:00:00Z"), ZoneOffset.UTC)

    @Test
    fun `creates new user when telegram id not found`() {
        val repository: UserJpaRepository = mock()
        whenever(repository.findByTelegramId(123L)).thenReturn(null)
        whenever(repository.saveAndFlush(any())).thenAnswer { invocation -> invocation.getArgument<UserEntity>(0) }

        val service = DefaultUserService(repository, clock)

        val result = service.upsertFromTelegram(telegramId = 123L, name = "Alice", avatarUrl = "  ")

        assertEquals("Alice", result.name)
        assertEquals(123L, result.telegramId)
        assertEquals(clock.instant(), result.createdAt)
        assertNotNull(result.id)
        verify(repository).saveAndFlush(any())
    }

    @Test
    fun `updates existing user when data differs`() {
        val existing = UserEntity(
            id = "user-1",
            name = "Old",
            avatarUrl = null,
            telegramId = 123L,
            createdAt = Instant.parse("2025-01-01T00:00:00Z"),
        )
        val repository: UserJpaRepository = mock()
        whenever(repository.findByTelegramId(123L)).thenReturn(existing)
        whenever(repository.saveAndFlush(any())).thenAnswer { invocation -> invocation.getArgument<UserEntity>(0) }

        val service = DefaultUserService(repository, clock)
        val result = service.upsertFromTelegram(telegramId = 123L, name = "New Name", avatarUrl = "http://avatar")

        assertEquals("New Name", result.name)
        assertEquals("http://avatar", result.avatarUrl)
        verify(repository).saveAndFlush(existing)
    }

    @Test
    fun `skips save when nothing changes`() {
        val existing = UserEntity(
            id = "user-1",
            name = "Same",
            avatarUrl = null,
            telegramId = 555L,
            createdAt = Instant.parse("2025-01-01T00:00:00Z"),
        )
        val repository: UserJpaRepository = mock()
        whenever(repository.findByTelegramId(555L)).thenReturn(existing)

        val service = DefaultUserService(repository, clock)
        val result = service.upsertFromTelegram(telegramId = 555L, name = "Same", avatarUrl = null)

        assertEquals("Same", result.name)
        verify(repository, never()).saveAndFlush(any())
        assertEquals(existing.createdAt, result.createdAt)
    }

    @Test
    fun `throws conflict when unique constraint violated`() {
        val repository: UserJpaRepository = mock()
        whenever(repository.findByTelegramId(777L)).thenReturn(null)
        whenever(repository.saveAndFlush(any())).thenThrow(DataIntegrityViolationException("dup"))

        val service = DefaultUserService(repository, clock)

        assertThrows(ConflictException::class.java) {
            service.upsertFromTelegram(telegramId = 777L, name = null, avatarUrl = null)
        }
    }
}
