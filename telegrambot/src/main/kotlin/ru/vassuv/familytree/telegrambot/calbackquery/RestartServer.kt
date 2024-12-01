package ru.vassuv.familytree.telegrambot.calbackquery

import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.entities.ChatId
import ru.vassuv.familytree.telegrambot.Consts

internal fun Dispatcher.restartServer() {
    callbackQuery("restartServer") {
        val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
        bot.sendMessage(ChatId.fromId(chatId), Consts.INFO)
    }
}