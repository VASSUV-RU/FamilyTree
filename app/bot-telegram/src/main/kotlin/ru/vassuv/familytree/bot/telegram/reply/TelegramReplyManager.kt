package ru.vassuv.familytree.bot.telegram.reply

import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
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
        val payload = when (reply) {
            is Reply.Text -> SendMessageRequest(chat_id = chatId, text = reply.text)
            is Reply.Buttons -> SendMessageRequest(
                chat_id = chatId,
                text = reply.text,
                reply_markup = InlineKeyboardMarkup(
                    inline_keyboard = reply.rows.map { row ->
                        row.map { b -> InlineKeyboardButton(text = b.text, url = b.url, callback_data = b.callbackData) }
                    }
                )
            )
        }

        return try {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            val entity = HttpEntity(payload, headers)
            val resp = rest.postForEntity(url, entity, Map::class.java)
            val ok = (resp.body as? Map<*, *>)?.get("ok") as? Boolean ?: (resp.statusCode.is2xxSuccessful)
            ReplyResult(ok = ok, description = resp.statusCode.toString())
        } catch (ex: Exception) {
            logger.error("Failed to send Telegram message: {}", ex.message)
            ReplyResult(ok = false, description = ex.message)
        }
    }
}

// --- Telegram API payloads ---
data class SendMessageRequest(
    val chat_id: Long,
    val text: String,
    val parse_mode: String? = null,
    val reply_markup: InlineKeyboardMarkup? = null,
)

data class InlineKeyboardMarkup(
    val inline_keyboard: List<List<InlineKeyboardButton>>,
)

data class InlineKeyboardButton(
    val text: String,
    val url: String? = null,
    val callback_data: String? = null,
)

