package ru.vassuv.familytree.server

import junit.framework.TestCase.assertTrue
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.PostgreSQLContainer
import ru.vassuv.familytree.request.FamilyCreateRequest
import ru.vassuv.familytree.request.UserCreateRequest
import ru.vassuv.familytree.server.db.DatabaseFactory
import ru.vassuv.familytree.server.repository.ExposedFamilyRepository
import ru.vassuv.familytree.server.repository.ExposedUserRepository
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DatabaseTest {

    val container = PostgreSQLContainer("postgres:15").apply {
        withDatabaseName("testdb")
        withUsername("test")
        withPassword("test")
    }

    @BeforeAll
    fun initDb() {
        container.start()
        DatabaseFactory.init(
            url = container.jdbcUrl,
            user = container.username,
            pass = container.password
        )
    }

    @Test
    fun `inserts and fetches user`() {
        val repo = ExposedUserRepository()
        val request = UserCreateRequest("TestUser", "test@user.com")
        repo.create(request)
        val all = repo.getAll()
        assertTrue(all.any { it.email == "test@user.com" })
    }

    @Test
    fun `inserts and fetches family`() {
        val repo = ExposedFamilyRepository()
        val request = FamilyCreateRequest("Сидоровы", 1)
        repo.create(request)
        val all = repo.getAll()
        assertTrue(all.any { it.name == "Сидоровы" })
    }

    @AfterAll
    fun tearDown() {
        container.stop()
    }
}
