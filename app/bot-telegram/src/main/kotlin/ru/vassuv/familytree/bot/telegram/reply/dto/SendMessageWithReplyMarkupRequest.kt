package ru.vassuv.familytree.bot.telegram.reply.dto

data class SendMessageWithReplyMarkupRequest(
    val chat_id: String,
    val text: String,
//    val parse_mode: String? = null,
    val reply_markup: InlineKeyboardMarkup? = null,
) {

    data class InlineKeyboardMarkup(
        val inline_keyboard: List<List<InlineKeyboardButton>>,
    )

    data class InlineKeyboardButton(
        val text: String,
        val callback_data: String? = null,
    )
}
