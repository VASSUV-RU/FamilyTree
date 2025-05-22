package ru.vassuv.familytree.server


import io.ktor.server.testing.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import ru.vassuv.familytree.User
import ru.vassuv.familytree.request.UserCreateRequest
import ru.vassuv.familytree.server.di.configModule
import ru.vassuv.familytree.server.di.repositoryModule
import ru.vassuv.familytree.server.di.routingModule
import ru.vassuv.familytree.server.di.serviceModule
import ru.vassuv.familytree.server.inmemoryrepo.InMemoryFamilyRepository
import ru.vassuv.familytree.server.inmemoryrepo.InMemoryUserRepository
import ru.vassuv.familytree.server.repository.FamilyRepository
import ru.vassuv.familytree.server.repository.UserRepository
import ru.vassuv.familytree.server.utils.testModule
import kotlin.test.*

class UserRoutesTest {
    private val testModule = module {
        single<UserRepository> {
            InMemoryUserRepository().apply {
                create(UserCreateRequest("Vasya", "vasya@a.b"))
                create(UserCreateRequest("Petya", "petya@b.c"))
            }
        }
        single<FamilyRepository> { InMemoryFamilyRepository() }
    }

    @BeforeTest
    fun setup() {
        startKoin {
            modules(configModule, repositoryModule, serviceModule, routingModule, testModule)
        }
    }

    @AfterTest
    fun teardown() {
        stopKoin()
    }

    @Test
    fun `GET users should return 200 with in-memory repo`() = testApplication {
        application { testModule() }

        val response = client.get("/users")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue("Vasya" in body)
        assertTrue("Petya" in body)
    }

    @Test
    fun `POST user with valid email returns 204`() = testApplication {
        application { testModule() }

        val response = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"Ivan", "email":"valid@email.ru"}""")
        }

        assertEquals(HttpStatusCode.Created, response.status)
    }
}