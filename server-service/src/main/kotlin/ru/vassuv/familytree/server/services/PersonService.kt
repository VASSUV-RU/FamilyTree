package ru.vassuv.familytree.server.services

import kotlinx.datetime.Clock
import ru.vassuv.familytree.Person
import ru.vassuv.familytree.server.NotFoundException
import ru.vassuv.familytree.server.ValidationException
import ru.vassuv.familytree.server.repository.PersonRepository

class PersonService(private val repository: PersonRepository) {

    fun getByFamily(familyId: Long): List<Person> = repository.getByFamily(familyId)

    fun getById(id: Long): Person =
        repository.getById(id) ?: throw NotFoundException("Person not found")

    fun addPerson(person: Person): Person {
        if (person.fullName.length !in 2..100) {
            throw ValidationException("Full name must be 2 to 100 characters long")
        }
        val now = Clock.System.now()
        return repository.create(
            person.copy(
                id = 0,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    fun linkUser(personId: Long, userId: Long): Person {
        return repository.linkUser(personId, userId)
    }
}