package ru.vassuv.familytree.server.repository

import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ru.vassuv.familytree.User
import ru.vassuv.familytree.request.UserCreateRequest
import ru.vassuv.familytree.server.db.UserDao
import ru.vassuv.familytree.server.db.UserTable


class ExposedUserRepository : UserRepository {
    override fun findByEmail(email: String): User? = transaction {
        UserDao.find { UserTable.email eq email }.firstOrNull()?.toModel()
    }

    override fun getAll(): List<User> = transaction {
        UserDao.all().map { it.toModel() }
    }

    override fun create(request: UserCreateRequest): User = transaction {
        val created = UserDao.new {
            name = request.name
            email = request.email
        }
        created.toModel()
    }
}