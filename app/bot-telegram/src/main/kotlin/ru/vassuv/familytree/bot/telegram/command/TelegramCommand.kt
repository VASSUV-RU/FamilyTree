package ru.vassuv.familytree.bot.telegram.command

import ru.vassuv.familytree.bot.telegram.webhook.dto.TelegramUpdateRequest

interface TelegramCommand {
    fun supports(update: TelegramUpdateRequest): Boolean
    fun execute(update: TelegramUpdateRequest): Any
}

