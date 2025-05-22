package ru.vassuv.familytree.server.db

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.date
import org.jetbrains.exposed.v1.datetime.timestamp
import ru.vassuv.familytree.Gender

internal object PersonTable : LongIdTable("persons") {
    val fullName = varchar("full_name", 100)
    val familyId = long("family_id")
    val userId = long("user_id").nullable()
    val birthDate = date("birth_date").nullable()
    val deathDate = date("death_date").nullable()
    val gender = enumerationByName("gender", 10, Gender::class).nullable()
    val notes = text("notes").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}