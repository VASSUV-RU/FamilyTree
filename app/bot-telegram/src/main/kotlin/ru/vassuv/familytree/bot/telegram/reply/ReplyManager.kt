package ru.vassuv.familytree.bot.telegram.reply

sealed interface Reply {
    data class Text(val text: String) : Reply
    data class Buttons(
        val text: String,
        val rows: List<List<Button>>
    ) : Reply {
        data class Button(
            val text: String,
            val url: String? = null,
            val callbackData: String? = null,
        )
    }
}

data class ReplyResult(val ok: Boolean, val description: String? = null)

interface ReplyManager {
    fun send(chatId: Long, reply: Reply): ReplyResult
}

