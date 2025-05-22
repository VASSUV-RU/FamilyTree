package ru.vassuv.familytree

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Person(
    val id: Long,
    val fullName: String,
    val familyId: Long,
    val userId: Long? = null,
    val birthDate: LocalDate? = null,
    val deathDate: LocalDate? = null,
    val gender: Gender? = null,
    val notes: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant
)

@Serializable
enum class Gender {
    MALE, FEMALE, OTHER
}