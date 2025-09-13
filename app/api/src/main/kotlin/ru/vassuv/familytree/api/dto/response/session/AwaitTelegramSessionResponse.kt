package ru.vassuv.familytree.api.dto.response.session

data class AwaitTelegramSessionResponse(
    val status: String,
    val auth: AuthTokensResponse? = null,
)

