package ru.vassuv.familytree.api.dto.request

data class CreateTelegramSessionRequest(
    val invitationId: String? = null,
)