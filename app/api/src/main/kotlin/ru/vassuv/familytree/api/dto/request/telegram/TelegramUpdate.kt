package ru.vassuv.familytree.api.dto.request.telegram

data class TelegramUpdate(
    val update_id: Long? = null,
    val message: TelegramMessage? = null,
)

data class TelegramMessage(
    val message_id: Long? = null,
    val text: String? = null,
    val from: TelegramFrom? = null,
    val chat: TelegramChat? = null,
)

data class TelegramFrom(
    val id: Long,
    val is_bot: Boolean? = null,
    val first_name: String? = null,
    val last_name: String? = null,
    val username: String? = null,
    val language_code: String? = null,
)

data class TelegramChat(
    val id: Long,
    val type: String? = null,
)

