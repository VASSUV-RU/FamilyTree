package ru.vassuv.familytree.server.repository

import kotlinx.datetime.Clock
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ru.vassuv.familytree.Family
import ru.vassuv.familytree.request.FamilyCreateRequest
import ru.vassuv.familytree.server.db.FamilyDao

class ExposedFamilyRepository : FamilyRepository {
    override fun getAll(): List<Family> = transaction {
        FamilyDao.all().map { it.toModel() }
    }

    override fun create(request: FamilyCreateRequest): Family = transaction {
        FamilyDao.new {
            val now = Clock.System.now()
            name = request.name
            creatorId = request.creatorId
            createdAt = now
            updatedAt = now
        }.toModel()
    }
}