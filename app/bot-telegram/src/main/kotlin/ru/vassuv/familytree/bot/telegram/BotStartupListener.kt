package ru.vassuv.familytree.bot.telegram

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class BotStartupListener(
    private val telegramBotService: TelegramBotMachine,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @EventListener(ApplicationReadyEvent::class)
    fun onReady() {
        logger.info("Application ready; initializing Telegram bot layer…")
    }
}

