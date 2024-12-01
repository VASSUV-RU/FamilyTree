package ru.vassuv.familytree.telegrambot.command

import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.ChatId
import ru.vassuv.familytree.telegrambot.Consts

internal fun Dispatcher.info() {
    command("info") {
        bot.sendMessage(chatId = ChatId.fromId(update.message!!.chat.id), text = Consts.INFO)
    }
}