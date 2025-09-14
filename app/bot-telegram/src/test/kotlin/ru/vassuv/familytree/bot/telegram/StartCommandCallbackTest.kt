package ru.vassuv.familytree.bot.telegram

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import ru.vassuv.familytree.bot.telegram.command.StartCommand
import ru.vassuv.familytree.bot.telegram.reply.ReplyManager
import ru.vassuv.familytree.bot.telegram.webhook.dto.TelegramCallbackQuery
import ru.vassuv.familytree.bot.telegram.webhook.dto.TelegramChat
import ru.vassuv.familytree.bot.telegram.webhook.dto.TelegramMessage
import ru.vassuv.familytree.bot.telegram.webhook.dto.TelegramUpdate
import ru.vassuv.familytree.bot.telegram.webhook.dto.TelegramUser
import ru.vassuv.familytree.service.auth.TelegramService

class StartCommandCallbackTest {

    @Test
    fun `handles register callback by sending help text`() {
        val svc: TelegramService = mock()
        val replies: ReplyManager = mock()
        val machine = TelegramBotMachine(listOf(StartCommand(svc, replies)))

        val update = TelegramUpdate(
            update_id = 101,
            callback_query = TelegramCallbackQuery(
                id = "cq1",
                from = TelegramUser(id = 999, username = "bob", first_name = "Bob", last_name = "S"),
                data = "register",
                message = TelegramMessage(
                    message_id = 55,
                    chat = TelegramChat(id = 100, type = "private")
                ),
            )
        )

        val res = machine.handle(update) as Map<*, *>
        assertEquals(true, res["ok"])

        // Should send a helper text back to the same chat
        verify(replies).send(org.mockito.kotlin.eq(100), org.mockito.kotlin.any())
    }

    @Test
    fun `handles ok callback with simple ack`() {
        val svc: TelegramService = mock()
        val replies: ReplyManager = mock()
        val machine = TelegramBotMachine(listOf(StartCommand(svc, replies)))

        val update = TelegramUpdate(
            update_id = 102,
            callback_query = TelegramCallbackQuery(
                id = "cq2",
                from = TelegramUser(id = 1000, username = "alice", first_name = "Alice", last_name = "K"),
                data = "ok",
                message = TelegramMessage(
                    message_id = 56,
                    chat = TelegramChat(id = 101, type = "private")
                ),
            )
        )

        val res = machine.handle(update) as Map<*, *>
        assertEquals(true, res["ok"])
    }
}

