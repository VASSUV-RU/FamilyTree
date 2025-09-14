package ru.vassuv.familytree.bot.telegram.reply.dto

data class SendMessageRequest(
    val chat_id: String,
    val text: String,
)