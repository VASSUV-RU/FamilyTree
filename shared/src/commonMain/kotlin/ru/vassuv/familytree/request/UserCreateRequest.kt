package ru.vassuv.familytree.request

import kotlinx.serialization.Serializable

@Serializable
data class UserCreateRequest(
    val name: String,
    val email: String
)