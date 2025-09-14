package ru.vassuv.familytree.bot.telegram.reply.mapping

import ru.vassuv.familytree.bot.telegram.reply.Reply
import ru.vassuv.familytree.bot.telegram.reply.dto.SendMessageRequest
import ru.vassuv.familytree.bot.telegram.reply.dto.SendMessageWithReplyMarkupRequest

fun Reply.toDto(chatId: Long) = when (this) {
  is Reply.Text -> mapToDto(chatId)
  is Reply.Buttons -> mapToDto(chatId)
}

private fun Reply.Text.mapToDto(chatId: Long) = SendMessageRequest(chat_id = chatId.toString(), text = text)

private fun Reply.Buttons.mapToDto(chatId: Long) = SendMessageWithReplyMarkupRequest(
  chat_id = chatId.toString(),
  text = text,
  reply_markup = SendMessageWithReplyMarkupRequest.InlineKeyboardMarkup(
    inline_keyboard = rows.map { row ->
      row.map { b ->
        SendMessageWithReplyMarkupRequest.InlineKeyboardButton(
          text = b.text,
          callback_data = b.callbackData
        )
      }
    }
  )
)