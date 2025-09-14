package ru.vassuv.familytree.bot.telegram

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.vassuv.familytree.bot.telegram.command.TelegramCommand
import ru.vassuv.familytree.bot.telegram.webhook.dto.TelegramUpdateRequest

@Component
class TelegramBotMachine(
    private val commands: List<TelegramCommand>,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun handle(update: TelegramUpdateRequest): Any {
        val text = update.message?.text
        if (text.isNullOrBlank()) return mapOf("ok" to true)

        val cmd = commands.firstOrNull { it.supports(update) }
        if (cmd == null) {
            logger.debug("No command matched; ignoring. text={}", text)
            return mapOf("ok" to true)
        }
        return cmd.execute(update)
    }
}
