package ru.vassuv.familytree.telegrambot.command

import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import ru.vassuv.familytree.telegrambot.Consts

internal fun Dispatcher.start() {
    command("start") {
        if (update.message!!.chat.id == Consts.TELEGRAM_BOT_MAIN_USER_ID) {
            val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
                listOf(InlineKeyboardButton.CallbackData(text = "Показать команды", callbackData = "info")),
                listOf(InlineKeyboardButton.CallbackData(text = "Перезапустить сервер", callbackData = "restartServer")),
                listOf(InlineKeyboardButton.CallbackData(text = "Собрать Api Сервер", callbackData = "buildApiServer")),
            )
            bot.sendMessage(
                chatId = ChatId.fromId(message.chat.id),
                text = "Доступные команды:",
                replyMarkup = inlineKeyboardMarkup,
            )
        } else {
            val inlineKeyboardMarkup = InlineKeyboardMarkup.createSingleButton(
                InlineKeyboardButton.CallbackData(text = "Давайте зарегистрируемся?", callbackData = "register"),
            )
            bot.sendMessage(
                chatId = ChatId.fromId(message.chat.id),
                text = "Здравствуйте - ${message.chat.firstName}",
                replyMarkup = inlineKeyboardMarkup,
            )
        }
            .fold(
                ifSuccess = { },
                ifError = { },
            )
    }
}