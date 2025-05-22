package ru.vassuv.familytree

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long = 0,
    val name: String,
    val email: String,
)
