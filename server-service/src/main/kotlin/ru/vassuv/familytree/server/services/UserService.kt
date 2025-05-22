package ru.vassuv.familytree.server.services

import ru.vassuv.familytree.User
import ru.vassuv.familytree.request.UserCreateRequest
import ru.vassuv.familytree.server.ConflictException
import ru.vassuv.familytree.server.ValidationException
import ru.vassuv.familytree.server.repository.UserRepository

class UserService(
    private val repository: UserRepository
) {
    fun getUsers(): List<User> = repository.getAll()

    fun addUser(request: UserCreateRequest) {
        if (request.name.length !in 2..100) {
            throw ValidationException("Name must be 2 to 100 chars long")
        }
        if (!request.email.contains("@")) {
            throw ValidationException("Invalid email format")
        }
        if (!request.email.contains(".")) {
            throw ValidationException("Email must contain domain")
        }

        if (repository.findByEmail(request.email) != null) {
            throw ConflictException("Email already exists")
        }
        repository.create(request)
    }
}