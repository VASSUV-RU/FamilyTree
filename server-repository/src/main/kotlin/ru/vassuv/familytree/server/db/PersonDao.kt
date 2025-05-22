package ru.vassuv.familytree.server.db

import ru.vassuv.familytree.Person
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

internal class PersonDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<PersonDao>(PersonTable)

    var fullName by PersonTable.fullName
    var familyId by PersonTable.familyId
    var userId by PersonTable.userId
    var birthDate by PersonTable.birthDate
    var deathDate by PersonTable.deathDate
    var gender by PersonTable.gender
    var notes by PersonTable.notes
    var createdAt by PersonTable.createdAt
    var updatedAt by PersonTable.updatedAt

    fun toModel() = Person(
        id = id.value,
        fullName = fullName,
        familyId = familyId,
        userId = userId,
        birthDate = birthDate,
        deathDate = deathDate,
        gender = gender,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
