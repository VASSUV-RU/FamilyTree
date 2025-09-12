package ru.vassuv.familytree.bot.telegram

import ru.vassuv.familytree.service.FamilyService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TelegramBotService(
    private val familyService: FamilyService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun notifyStartup() {
        // Placeholder: integrate telegram bot here later
        logger.info("Telegram bot layer initialized; people count={} ", familyService.countPeople())
    }
}

