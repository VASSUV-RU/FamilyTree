package ru.vassuv.familytree.server.services

import ru.vassuv.familytree.Family
import ru.vassuv.familytree.request.FamilyCreateRequest
import ru.vassuv.familytree.server.ValidationException
import ru.vassuv.familytree.server.repository.FamilyRepository

class FamilyService(private val repository: FamilyRepository) {

    fun getFamilies(): List<Family> = repository.getAll()

    fun addFamily(request: FamilyCreateRequest) {
        if (request.name.length !in 2..100) throw ValidationException("Family name invalid")
        repository.create(request)
    }
}