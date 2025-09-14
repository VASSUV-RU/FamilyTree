package ru.vassuv.familytree.bot.telegram

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import ru.vassuv.familytree.bot.telegram.command.PingCommand
import ru.vassuv.familytree.bot.telegram.command.StartCommand
import ru.vassuv.familytree.bot.telegram.reply.ReplyManager
import ru.vassuv.familytree.bot.telegram.webhook.dto.TelegramChat
import ru.vassuv.familytree.bot.telegram.webhook.dto.TelegramMessage
import ru.vassuv.familytree.bot.telegram.webhook.dto.TelegramUpdate
import ru.vassuv.familytree.bot.telegram.webhook.dto.TelegramUser
import ru.vassuv.familytree.service.auth.TelegramService

class TelegramBotMachineTest {

    @Test
    fun `handles start and confirms via service`() {
        val svc: TelegramService = mock()
        val replies: ReplyManager = mock()
        val machine = TelegramBotMachine(listOf(StartCommand(svc, replies)))

        whenever(svc.parseStartSid("/start Sabc")).thenReturn("Sabc")
        whenever(
            svc.confirmStart(eq("Sabc"), any())
        ).thenReturn(TelegramService.WebhookConfirmResult(true, "Session confirmed. You can return to app."))

        val update = TelegramUpdate(
            update_id = 1,
            message = TelegramMessage(
                message_id = 10,
                text = "/start Sabc",
                chat = TelegramChat(id = 100, type = "private"),
                from = TelegramUser(id = 999, username = "bob", first_name = "Bob", last_name = "S")
            )
        )

        val res = machine.handle(update) as Map<*, *>
        assertEquals(true, res["ok"])
        assertEquals("Session confirmed. You can return to app.", res["message"])

        verify(svc).parseStartSid("/start Sabc")
        verify(svc).confirmStart(eq("Sabc"), any())
    }

    @Test
    fun `ignores non-start messages`() {
        val svc: TelegramService = mock()
        val replies: ReplyManager = mock()
        val machine = TelegramBotMachine(listOf(StartCommand(svc, replies)))

        val update = TelegramUpdate(
            update_id = 2,
            message = TelegramMessage(
                message_id = 11,
                text = "hello",
                chat = TelegramChat(id = 100, type = "private"),
                from = TelegramUser(id = 999, username = "bob", first_name = "Bob", last_name = "S")
            )
        )

        val res = machine.handle(update) as Map<*, *>
        assertEquals(true, res["ok"])
    }
    
    @Test
    fun `handles ping command`() {
        val svc: TelegramService = mock() // not used by Ping
        val replies: ReplyManager = mock()
        val machine = TelegramBotMachine(listOf(PingCommand(replies), StartCommand(svc, replies)))

        val update = TelegramUpdate(
            update_id = 3,
            message = TelegramMessage(
                message_id = 12,
                text = "/ping",
                chat = TelegramChat(id = 100, type = "private"),
                from = TelegramUser(id = 999, username = "bob", first_name = "Bob", last_name = "S")
            )
        )

        val res = machine.handle(update) as Map<*, *>
        assertEquals(true, res["ok"])
        assertEquals("pong", res["message"])
    }
}
