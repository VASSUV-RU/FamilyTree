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
        val callbackData = update.callback_query?.data
        if (text.isNullOrBlank() && callbackData.isNullOrBlank()) return mapOf("ok" to true)

        val cmd = commands.firstOrNull { it.supports(update) }
        if (cmd == null) {
            logger.debug("No command matched; ignoring. text={}, callback={}", text, callbackData)
            return mapOf("ok" to true)
        }
        return cmd.execute(update)
    }
}
