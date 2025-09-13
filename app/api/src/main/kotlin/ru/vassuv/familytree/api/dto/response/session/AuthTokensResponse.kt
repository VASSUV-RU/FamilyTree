package ru.vassuv.familytree.api.dto.response.session

data class AuthTokensResponse(
    val accessToken: String,
    val refreshToken: String,
)

