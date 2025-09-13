package ru.vassuv.familytree.api.controller

import org.springframework.web.bind.annotation.*
import ru.vassuv.familytree.api.dto.request.telegram.TelegramUpdate
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
        webhookService.validateSecretOrThrow(secret, props.webhookSecret)

        val text = update.message?.text ?: return mapOf("ok" to true)
        val from = update.message.from ?: return mapOf("ok" to true)

        val sid = webhookService.parseStartSid(text) ?: return mapOf("ok" to true)
        val tg = TelegramUserInfo(
            id = from.id,
            username = from.username,
            firstName = from.first_name,
            lastName = from.last_name,
        )
        val result = webhookService.confirmStart(sid, tg)
        return mapOf("ok" to result.ok, "message" to result.message)
    }

    // parsing moved to service
}
