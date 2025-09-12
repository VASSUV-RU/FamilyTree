package ru.vassuv.familytree.service

import ru.vassuv.familytree.data.repo.PersonRepository
import org.springframework.stereotype.Service

@Service
class FamilyService(
    private val personRepository: PersonRepository,
) {
    fun countPeople(): Long = personRepository.countUsers()
}

