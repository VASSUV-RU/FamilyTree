package ru.vassuv.familytree.service.model

data class CreatedTelegramSession(
  val sid: String,
  val expiresIn: Long,
)