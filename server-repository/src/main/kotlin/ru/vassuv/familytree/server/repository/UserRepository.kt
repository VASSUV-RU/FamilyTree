package ru.vassuv.familytree.server.repository

import ru.vassuv.familytree.User
import ru.vassuv.familytree.request.UserCreateRequest

interface UserRepository {
    fun findByEmail(email: String): User?
    fun getAll(): List<User>
    fun create(request: UserCreateRequest): User
}