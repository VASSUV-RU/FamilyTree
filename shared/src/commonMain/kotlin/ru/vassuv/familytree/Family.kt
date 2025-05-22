package ru.vassuv.familytree

import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName

@Serializable
data class Family(
    val id: Long,
    val name: String,
    @SerialName("creator_id")
    val creatorId: Long,
    @SerialName("created_at")
    val createdAt: Instant,
    @SerialName("updated_at")
    val updatedAt: Instant,
)
