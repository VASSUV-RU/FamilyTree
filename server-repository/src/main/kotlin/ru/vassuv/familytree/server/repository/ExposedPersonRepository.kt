package ru.vassuv.familytree.server.repository

import kotlinx.datetime.Clock
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ru.vassuv.familytree.Person
import ru.vassuv.familytree.server.db.PersonDao
import ru.vassuv.familytree.server.db.PersonTable

class ExposedPersonRepository : PersonRepository {
    override fun getByFamily(familyId: Long): List<Person> = transaction {
        PersonDao.find { PersonTable.familyId eq familyId }
            .map { it.toModel() }
    }

    override fun getById(id: Long): Person? = transaction {
        PersonDao.findById(id)?.toModel()
    }

    override fun create(person: Person): Person = transaction {
        PersonDao.new {
            fullName = person.fullName
            familyId = person.familyId
            userId = person.userId
            birthDate = person.birthDate
            deathDate = person.deathDate
            gender = person.gender
            notes = person.notes
            createdAt = person.createdAt
            updatedAt = person.updatedAt
        }.toModel()
    }

    override fun linkUser(personId: Long, userId: Long): Person = transaction {
        val person = PersonDao.findById(personId) ?: error("Person not found")
        person.userId = userId
        person.updatedAt = Clock.System.now()
        person.toModel()
    }
}
