package ru.vassuv.familytree.bot.telegram.command

import org.springframework.stereotype.Component
import ru.vassuv.familytree.bot.telegram.webhook.dto.TelegramUpdateRequest

@Component
class PingCommand : TelegramCommand {
    override fun supports(update: TelegramUpdateRequest): Boolean =
        update.message?.text?.trim()?.equals("/ping", ignoreCase = true) == true

    override fun execute(update: TelegramUpdateRequest): Any =
        mapOf("ok" to true, "message" to "pong")
}

