package ru.vassuv.familytree.api.dto.request

import jakarta.validation.constraints.NotBlank

data class RefreshRequest(
    @field:NotBlank
    val refreshToken: String,
)
