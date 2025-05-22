package ru.vassuv.familytree.server.db

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestamp

internal object FamilyTable : LongIdTable("families") {
    val name = varchar("name", 100)
    val creatorId = long("creator_id")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}