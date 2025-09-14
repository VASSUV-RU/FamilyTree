package ru.vassuv.familytree.bot.telegram

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.vassuv.familytree.bot.telegram.webhook.dto.TelegramUpdateRequest
import ru.vassuv.familytree.service.auth.TelegramService

@Component
class TelegramBotMachine(
    private val service: TelegramService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun handle(update: TelegramUpdateRequest): Any {
        val text = update.message?.text ?: return mapOf("ok" to true)
        val from = update.message.from ?: return mapOf("ok" to true)

        val sid = service.parseStartSid(text) ?: run {
            logger.debug("Incoming message without /start sid; ignoring. uid={}", from.id)
            return mapOf("ok" to true, "message" to "Приветствую")
        }
        val tg = TelegramService.TelegramUserInfo(
            id = from.id,
            username = from.username,
            firstName = from.first_name,
            lastName = from.last_name,
        )
        val result = service.confirmStart(sid, tg)
        logger.info("Confirm start: sid={}, uid={}, ok={} msg={}", sid, from.id, result.ok, result.message)
        return mapOf("ok" to result.ok, "message" to result.message)
    }
}
