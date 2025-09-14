package ru.vassuv.familytree.bot.telegram.command

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.vassuv.familytree.bot.telegram.reply.Reply
import ru.vassuv.familytree.bot.telegram.reply.ReplyManager
import ru.vassuv.familytree.bot.telegram.webhook.dto.TelegramUpdateRequest
import ru.vassuv.familytree.service.auth.TelegramService

@Component
class StartCommand(
    private val service: TelegramService,
    private val replies: ReplyManager,
) : TelegramCommand {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun supports(update: TelegramUpdateRequest): Boolean {
        val text = update.message?.text
        val data = update.callback_query?.data
        return when {
            text != null && text.trim().startsWith("/start") -> true
            data != null && (data == "register") -> true
            else -> false
        }
    }

    override fun execute(update: TelegramUpdateRequest): Any {
        // Handle callbacks from inline buttons first
        update.callback_query?.let { cq ->
            val chatId = cq.message?.chat?.id
            val data = cq.data
            logger.debug("Handling callback for StartCommand: uid={}, data={}", cq.from.id, data)
            when (data) {
                "register" -> {
                    if (chatId != null) {
                        replies.send(chatId, Reply.Text("""Регистрация прошла успешно. Поздравляем!!!
                            username - ${cq.from.username}
                            firstName - ${cq.from.first_name}
                            lastName - ${cq.from.last_name}
                            isBot - ${cq.from.is_bot}
                            Можете переходить в сервис (https://examle.com)""".trimIndent()))
                    }
                    return mapOf("ok" to true)
                }
                "ok" -> {
                    // Nothing else to do; confirmation already sent with message
                    return mapOf("ok" to true)
                }
            }
            return mapOf("ok" to true)
        }

        val text = update.message?.text ?: return mapOf("ok" to true)
        val from = update.message.from ?: return mapOf("ok" to true)
        val chatId = update.message.chat.id

        val sid = service.parseStartSid(text) ?: run {
            logger.debug("/start without sid; ignoring. uid={}", from.id)
            val btn = Reply.Buttons.Button(text = "Зарегистрироваться", callbackData = "register")
            replies.send(chatId, Reply.Buttons(text = "Чтобы воспользоваться сервисом нужно зарегистрироваться в нем", rows = listOf(listOf(btn))))
            return mapOf("ok" to true)
        }

        val tg = TelegramService.TelegramUserInfo(
            id = from.id,
            username = from.username,
            firstName = from.first_name,
            lastName = from.last_name,
        )
        val result = service.confirmStart(sid, tg)
        logger.info("Confirm start: sid={}, uid={}, ok={} msg={}", sid, from.id, result.ok, result.message)
        // send confirmation with a button
        val btn = Reply.Buttons.Button(text = "OK", callbackData = "ok")
        replies.send(chatId, Reply.Buttons(text = result.message, rows = listOf(listOf(btn))))
        return mapOf("ok" to result.ok, "message" to result.message)
    }
}
