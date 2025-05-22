package ru.vassuv.familytree.server.db

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

internal object UserTable : LongIdTable("users") {
    val name = varchar("name", 100)
    val email = varchar("email", 100).uniqueIndex()
}