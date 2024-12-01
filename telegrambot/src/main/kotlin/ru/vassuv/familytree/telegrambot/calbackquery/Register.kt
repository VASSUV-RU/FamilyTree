package ru.vassuv.familytree.telegrambot.calbackquery

import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.entities.ChatId
import ru.vassuv.familytree.telegrambot.Consts

internal fun Dispatcher.register() {
    callbackQuery("register") {
        val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
        bot.sendMessage(ChatId.fromId(chatId), "Вы зарегистрированы - ${callbackQuery.message?.chat?.firstName}")
        bot.sendMessage(
            ChatId.fromId(Consts.TELEGRAM_BOT_MAIN_USER_ID),
            "Зарегистрировался - ${callbackQuery.message?.chat?.firstName} ${callbackQuery.message?.chat?.lastName} (@${callbackQuery.message?.chat?.username})"
        )
    }
}