package ru.vassuv.familytree.server.inmemoryrepo

import kotlinx.datetime.Clock
import ru.vassuv.familytree.Family
import ru.vassuv.familytree.request.FamilyCreateRequest
import ru.vassuv.familytree.server.repository.FamilyRepository
import java.util.concurrent.atomic.AtomicLong

class InMemoryFamilyRepository : FamilyRepository {
    private val idCounter = AtomicLong(1)
    private val list = mutableListOf<Family>()

    override fun getAll(): List<Family> = list.toList()

    override fun create(request: FamilyCreateRequest): Family {
        val created = Family(
            id = idCounter.getAndIncrement(),
            name = request.name,
            creatorId = request.creatorId,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
        list.add(created)
        return created
    }
}