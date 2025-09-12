package ru.vassuv.familytree.api.controller

import org.springframework.web.bind.annotation.*
import ru.vassuv.familytree.api.controller.dto.request.telegram.TelegramUpdate
import ru.vassuv.familytree.api.exception.unauthorizeError
import ru.vassuv.familytree.config.TelegramAuthProperties
import ru.vassuv.familytree.service.auth.TelegramUserInfo
import ru.vassuv.familytree.service.auth.TelegramWebhookService

@RestController
@RequestMapping("/auth/telegram")
class TelegramWebhookController(
    private val webhookService: TelegramWebhookService,
    private val props: TelegramAuthProperties,
) {

    @PostMapping("/webhook")
    fun handleWebhook(
        @RequestBody update: TelegramUpdate,
        @RequestHeader(name = "X-Telegram-Bot-Api-Secret-Token", required = false) secret: String?,
    ): Any {
        if (props.webhookSecret.isBlank() || secret == null || secret != props.webhookSecret) {
            unauthorizeError()
        }

        val text = update.message?.text ?: return mapOf("ok" to true)
        val from = update.message.from ?: return mapOf("ok" to true)

        val sid = parseSid(text) ?: return mapOf("ok" to true)
        val tg = TelegramUserInfo(
            id = from.id,
            username = from.username,
            firstName = from.first_name,
            lastName = from.last_name,
        )
        val result = webhookService.confirmStart(sid, tg)
        return mapOf("ok" to result.ok, "message" to result.message)
    }

    private fun parseSid(text: String): String? {
        // Accept variants: "/start <sid>", "/start@bot <sid>", any extra spaces trimmed
        val t = text.trim()
        if (!t.startsWith("/start")) return null
        val parts = t.split(Regex("\\s+"))
        return if (parts.size >= 2) parts[1] else null
    }
}
