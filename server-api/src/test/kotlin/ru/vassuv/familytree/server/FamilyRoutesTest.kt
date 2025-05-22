package ru.vassuv.familytree.server

import io.ktor.server.testing.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import ru.vassuv.familytree.request.FamilyCreateRequest
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

class FamilyRoutesTest {

    private val testModule = module {
        single<UserRepository> { InMemoryUserRepository() }
        single<FamilyRepository> {
            InMemoryFamilyRepository().apply {
                create(FamilyCreateRequest("Сидоровы", 1))
            }
        }
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
    fun `GET families returns 200 and content`() = testApplication {
        application { testModule() }

        val response = client.get("/families")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Сидоровы"))
    }

    @Test
    fun `POST valid family returns 201`() = testApplication {
        application { testModule() }

        val response = client.post("/families") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"Петровы", "creator_id":1}""")
        }

        assertEquals(HttpStatusCode.Created, response.status)
    }
}