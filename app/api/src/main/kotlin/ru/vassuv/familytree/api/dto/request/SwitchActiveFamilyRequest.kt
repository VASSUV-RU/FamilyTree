package ru.vassuv.familytree.api.dto.request

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class SwitchActiveFamilyRequest(
    @field:NotNull
    @field:Positive
    val familyId: Long?,
)

