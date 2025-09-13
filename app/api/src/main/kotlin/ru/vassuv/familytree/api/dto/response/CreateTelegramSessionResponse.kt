package ru.vassuv.familytree.api.dto.response

data class CreateTelegramSessionResponse(
    val sid: String,
    val deeplinkUrl: String,
    val expiresIn: Long,
)