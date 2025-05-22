package ru.vassuv.familytree.server.repository

import ru.vassuv.familytree.Family
import ru.vassuv.familytree.request.FamilyCreateRequest

interface FamilyRepository {
    fun getAll(): List<Family>
    fun create(request: FamilyCreateRequest): Family
}