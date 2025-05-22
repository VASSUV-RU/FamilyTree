package ru.vassuv.familytree.server.db

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import ru.vassuv.familytree.Family

internal class FamilyDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<FamilyDao>(FamilyTable)

    var name by FamilyTable.name
    var creatorId by FamilyTable.creatorId
    var createdAt by FamilyTable.createdAt
    var updatedAt by FamilyTable.updatedAt

    fun toModel() = Family(
        id.value,
        name,
        creatorId,
        createdAt,
        updatedAt,
    )
}