package ru.vassuv.familytree.bot.telegram.reply

import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import ru.vassuv.familytree.bot.telegram.reply.mapping.toDto
import ru.vassuv.familytree.config.TelegramAuthProperties

@Component
class TelegramReplyManager(
    private val props: TelegramAuthProperties,
) : ReplyManager {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val rest = RestTemplate()

    override fun send(chatId: Long, reply: Reply): ReplyResult {
        val token = props.botToken
        if (token.isNullOrBlank()) {
            logger.warn("Telegram bot token is not configured; skipping send. chatId={}", chatId)
            return ReplyResult(ok = false, description = "Bot token not configured")
        }

        val url = "https://api.telegram.org/bot${token}/sendMessage"
        val payload = reply.toDto(chatId)

        return try {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            val entity = HttpEntity(payload, headers)
            val resp = rest.postForEntity<Map<String, String>>(url, entity)
            val ok = (resp.body as? Map<*, *>)?.get("ok") as? Boolean ?: (resp.statusCode.is2xxSuccessful)
            ReplyResult(ok = ok, description = resp.statusCode.toString())
        } catch (ex: Exception) {
            logger.error("Failed to send Telegram message: {}", ex.message)
            ReplyResult(ok = false, description = ex.message)
        }
    }
}