package ru.vassuv.familytree.server.repository

import ru.vassuv.familytree.Person

interface PersonRepository {
    fun getByFamily(familyId: Long): List<Person>
    fun getById(id: Long): Person?
    fun create(person: Person): Person
    fun linkUser(personId: Long, userId: Long): Person
}