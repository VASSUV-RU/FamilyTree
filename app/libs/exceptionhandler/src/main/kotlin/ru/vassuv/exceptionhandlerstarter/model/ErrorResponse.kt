package ru.vassuv.exceptionhandlerstarter.model

import java.time.LocalDateTime

/**
 * Структура данных, используемая для возвращения подробной информации об ошибках.
 *
 * @property status HTTP-статус ошибки.
 * @property error Короткое описание ошибки.
 * @property message Детальное сообщение об ошибке.
 * @property classPath Путь к классу и методу, вызвавшим ошибку.
 * @property originalMessage Исходное сообщение об ошибке (необязательно).
 * @property timestamp Время возникновения ошибки.
 */
data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String?,
    val classPath: String,
    val originalMessage: String? = null,
    val timestamp: LocalDateTime = LocalDateTime.now(),
)
