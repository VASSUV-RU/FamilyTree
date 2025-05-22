package ru.vassuv.familytree.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FamilyCreateRequest(
    val name: String,
    @SerialName("creator_id")
    val creatorId: Long
)