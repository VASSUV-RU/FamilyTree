package ru.vassuv.familytree.api.controller.dto.response

data class CreateTelegramSessionResponse(
    val sid: String,
    val deeplinkUrl: String,
    val expiresIn: Long,
)