package ru.vassuv.familytree.server

import kotlin.test.*
import org.junit.jupiter.api.Test
import ru.vassuv.familytree.request.UserCreateRequest
import ru.vassuv.familytree.server.inmemoryrepo.InMemoryUserRepository
import ru.vassuv.familytree.server.services.UserService

class UserServiceTest {

    private val repository = InMemoryUserRepository()
    private val service = UserService(repository)

    @Test
    fun `adding user with invalid name throws ValidationException`() {
        val invalid = UserCreateRequest("", "alice@a.b")
        val ex = assertFailsWith<ValidationException> {
            service.addUser(invalid)
        }
        assertTrue("Name must be 2 to 100 chars long" in ex.message.orEmpty())
    }

    @Test
    fun `adding user with invalid email throws ValidationException`() {
        val user = UserCreateRequest("Alice", "alice")
        val ex = assertFailsWith<ValidationException> {
            service.addUser(user)
        }
        assertTrue("Invalid email format" in ex.message.orEmpty())
    }

    @Test
    fun `adding user with invalid email another throws ValidationException`() {
        val user = UserCreateRequest("Alice", "alice@a")
        val ex = assertFailsWith<ValidationException> {
            service.addUser(user)
        }
        assertTrue("Email must contain domain" in ex.message.orEmpty())
    }

    @Test
    fun `adding user with existing email throws ConflictException`() {
        val user = UserCreateRequest("Alice", "alice@a.b")
        service.addUser(user)

        val ex = assertFailsWith<ConflictException> {
            service.addUser(user)
        }
        assertTrue("Email already exists" in ex.message.orEmpty())
    }

    @Test
    fun `getUsers returns correct list`() {
        val user = UserCreateRequest("Bob", "bob@a.b")
        service.addUser(user)
        val all = service.getUsers()
        assertEquals(1, all.size)
        assertEquals("Bob", all[0].name)
    }
}