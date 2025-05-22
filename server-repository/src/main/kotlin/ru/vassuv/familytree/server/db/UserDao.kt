package ru.vassuv.familytree.server.db

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import ru.vassuv.familytree.User

internal class UserDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserDao>(UserTable)

    var name by UserTable.name
    var email by UserTable.email

    fun toModel() = User(id.value, name, email)
}