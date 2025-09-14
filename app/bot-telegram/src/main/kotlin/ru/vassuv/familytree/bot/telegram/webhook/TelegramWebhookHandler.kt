package ru.vassuv.familytree.bot.telegram.webhook

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.vassuv.familytree.bot.telegram.TelegramBotMachine
import ru.vassuv.familytree.bot.telegram.webhook.dto.TelegramUpdateRequest

@Service
class TelegramWebhookHandler(
    private val botMachine: TelegramBotMachine,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun handle(update: TelegramUpdateRequest) {
        try {
            when {
                update.message != null -> handleMessage(update)
                update.callback_query != null -> handleCallback(update)
                else -> logger.debug("Ignored update: {}", update.update_id)
            }
        } catch (ex: Exception) {
            logger.error("Failed to handle Telegram update {}", update.update_id, ex)
            // Intentionally swallow to always return 200 to Telegram
        }
    }

    private fun handleMessage(update: TelegramUpdateRequest) {
        botMachine.handle(update)
    }

    private fun handleCallback(update: TelegramUpdateRequest) {
        val cq = update.callback_query ?: return
        logger.debug("Callback query from user={}, data={}", cq.from.id, cq.data)
    }
}
