package ru.vassuv.familytree.bot.telegram.webhook.dto

data class TelegramUpdateRequest (
    val update_id: Long,
    val message: TelegramMessage? = null,
    val edited_message: TelegramMessage? = null,
    val callback_query: TelegramCallbackQuery? = null,
)

// Align with tests: expose expected name
typealias TelegramUpdate = TelegramUpdateRequest

data class TelegramMessage(
    val message_id: Long,
    val date: Long? = null,
    val text: String? = null,
    val chat: TelegramChat,
    val from: TelegramUser? = null,
)

data class TelegramChat(
    val id: Long,
    val type: String? = null,
    val title: String? = null,
    val username: String? = null,
    val first_name: String? = null,
    val last_name: String? = null,
)

data class TelegramUser(
    val id: Long,
    val is_bot: Boolean? = null,
    val first_name: String? = null,
    val last_name: String? = null,
    val username: String? = null,
    val language_code: String? = null,
)

data class TelegramCallbackQuery(
    val id: String,
    val from: TelegramUser,
    val message: TelegramMessage? = null,
    val data: String? = null,
)
