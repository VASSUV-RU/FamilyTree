package ru.vassuv.familytree.server

import kotlin.test.*
import org.junit.jupiter.api.Test
import ru.vassuv.familytree.request.FamilyCreateRequest
import ru.vassuv.familytree.server.inmemoryrepo.InMemoryFamilyRepository
import ru.vassuv.familytree.server.services.FamilyService

class FamilyServiceTest {

    private val repo = InMemoryFamilyRepository()
    private val service = FamilyService(repo)

    @Test
    fun `creating valid family works`() {
        val request = FamilyCreateRequest("Ивановы", 1)
        service.addFamily(request)
        val all = service.getFamilies()
        assertTrue(all.any { it.name == "Ивановы" })
    }

    @Test
    fun `creating family with short name throws`() {
        val request = FamilyCreateRequest( "A", 1)
        val ex = assertFailsWith<ValidationException> {
            service.addFamily(request)
        }
        assertTrue(ex.message!!.contains("Family name"))
    }
}