package ru.vassuv.familytree.service.model

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
)

