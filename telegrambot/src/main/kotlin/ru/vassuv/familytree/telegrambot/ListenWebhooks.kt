package ru.vassuv.familytree.telegrambot

import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.channel
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton

internal fun Dispatcher.listenWebhooks() {
    var commandsMessageId: Long? = null

    channel {
        if (this.channelPost.authorSignature != "FamilyTreeWebhookBot") {
            return@channel
        }

        val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
            listOf(
                InlineKeyboardButton.CallbackData(text = "Показать команды", callbackData = "info"),
                InlineKeyboardButton.CallbackData(text = "Перезапустить сервер", callbackData = "restartServer")
            ),
            listOf(
                InlineKeyboardButton.CallbackData(text = "Собрать Api Сервер", callbackData = "buildApiServer"),
                InlineKeyboardButton.CallbackData(text = "Deploy Api Сервер", callbackData = "deployApiServer"),
            ),
//            listOf(
//                InlineKeyboardButton.CallbackData(text = "Собрать Webhook Сервер", callbackData = "buildApiServer"),
//                InlineKeyboardButton.CallbackData(text = "Deploy Webhook Сервер", callbackData = "buildApiServer"),
//            ),
            listOf(
                InlineKeyboardButton.CallbackData(text = "Собрать Telegram Bot", callbackData = "buildTelegramServer"),
                InlineKeyboardButton.CallbackData(text = "Deploy Telegram Bot", callbackData = "deployTelegramServer"),
            ),
            listOf(
                InlineKeyboardButton.CallbackData(text = "Собрать Web", callbackData = "buildWeb"),
                InlineKeyboardButton.CallbackData(text = "Deploy Web", callbackData = "deployWeb"),
            ),
        )

        commandsMessageId?.let {
            bot.deleteMessage(
                chatId = ChatId.fromId(Consts.TELEGRAM_BOT_CHANNEL_ID),
                messageId = it
            )
        }

        bot.sendMessage(
            chatId = ChatId.fromId(Consts.TELEGRAM_BOT_CHANNEL_ID),
            text = "Доступные команды:",
            replyMarkup = inlineKeyboardMarkup,
        ).onSuccess { commandsMessageId = it.messageId }
        println("Получен сигнал с вебхука")
    }

    message(
//                Filter.Chat(Consts.TELEGRAM_DEPLOY_BOT_CHANNEL_ID) or
//                        Filter.User(Consts.TELEGRAM_DEPLOY_BOT_ID)
    ) {
        val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
            listOf(InlineKeyboardButton.CallbackData(text = "Показать команды", callbackData = "info")),
            listOf(InlineKeyboardButton.CallbackData(text = "Перезапустить сервер", callbackData = "restartServer")),
            listOf(InlineKeyboardButton.CallbackData(text = "Собрать Api Сервер", callbackData = "buildApiServer")),
        )
        bot.sendMessage(
            chatId = ChatId.fromId(Consts.TELEGRAM_BOT_CHANNEL_ID),
            text = "Доступные команды:",
            replyMarkup = inlineKeyboardMarkup,
        ).onSuccess { commandsMessageId = it.messageId }

        commandsMessageId?.let {
            bot.deleteMessage(
                chatId = ChatId.fromId(Consts.TELEGRAM_BOT_CHANNEL_ID),
                messageId = it
            )
        }
    }
}