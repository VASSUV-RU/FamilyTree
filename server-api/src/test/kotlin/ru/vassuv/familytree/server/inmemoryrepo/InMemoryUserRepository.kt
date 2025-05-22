package ru.vassuv.familytree.server.inmemoryrepo

import ru.vassuv.familytree.User
import ru.vassuv.familytree.request.UserCreateRequest
import ru.vassuv.familytree.server.repository.UserRepository
import java.util.concurrent.atomic.AtomicLong

class InMemoryUserRepository : UserRepository {
    private val idCounter = AtomicLong(1)
    private val users = mutableListOf<User>()

    override fun findByEmail(email: String): User? =
        users.find { it.email == email }

    override fun getAll(): List<User> = users.toList()

    override fun create(request: UserCreateRequest): User {
        val created = request.toUser()
        users.add(created)
        return created
    }

    private fun UserCreateRequest.toUser() = User(
        email = email,
        name = name,
        id = idCounter.getAndIncrement(),
    )
}