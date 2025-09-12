package ru.vassuv.exceptionhandlerstarter.handler

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import ru.vassuv.exceptionhandlerstarter.model.ErrorResponse
import java.util.logging.Logger
import kotlin.reflect.KAnnotatedElement

/**
 * Глобальный обработчик исключений.
 * Перехватывает ошибки и возвращает пользователю удобные сообщения.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    init {
        println("✅ GlobalExceptionHandler from starter initialized")
    }

    /**
     * Обработка всех исключений приложения.
     *
     * @param ex Исключение, возникающее при ошибке.
     * @return Ответ с описанием ошибок.
     */
    @ExceptionHandler(Exception::class)
    fun handleAllUncaughtException(ex: Exception): ResponseEntity<ErrorResponse> {
        val annotationStatus = ex.resolveAnnotatedStatus() ?: HttpStatus.INTERNAL_SERVER_ERROR
        val statusCode = annotationStatus.value()
        val statusName = annotationStatus.name
        val classPath = ex.classPath
        val apiError = ErrorResponse(
            statusCode,
            statusName,
            ex.localizedMessage,
            classPath,
            originalMessage = ex.cause.toString()
        )
            .also { logError(it) }

        return ResponseEntity(apiError, annotationStatus)
    }

    /**
     * Обрабатывает исключения валидации.
     *
     * @param ex Исключение, возникающее при ошибке валидации.
     * @return Ответ с описанием ошибок.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ErrorResponse {
        val errors = ex.bindingResult.allErrors.associate { error ->
            val fieldName = (error as FieldError).field
            fieldName to error.defaultMessage
        }
        val status = HttpStatus.BAD_REQUEST
        val classPath = ex.classPath
        val message = when (errors.size) {
            0 -> null
            1 -> "Не правильно переданное значение в поле: ${errors.keys.first()}"
            else -> "Не правильно переданные значения в полях: ${errors.keys.joinToString(",")}"
        }

        return ErrorResponse(status.value(), status.name, message, classPath)
            .also { logError(it) }
    }

    /**
     * Обрабатывает исключения парсинга json.
     *
     * @param ex Исключение, возникающее при ошибке парсинга.
     * @return Ответ с описанием ошибок.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleJsonParseException(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        val rootCause = ex.cause
        var originalMessage: String? = null
        val detailedMessage = when (rootCause) {
            is com.fasterxml.jackson.databind.exc.InvalidFormatException -> {
                val fieldPath = rootCause.path.joinToString(".") { it.fieldName }
                originalMessage = rootCause.originalMessage
                "Некорректный формат поля '$fieldPath'"
            }

            is com.fasterxml.jackson.databind.exc.MismatchedInputException -> {
                val fieldPath = rootCause.path.joinToString(".") { it.fieldName ?: "get(*)" }
                originalMessage = rootCause.originalMessage
                "Неправильный тип данных поля '$fieldPath'"
            }

            else -> ex.localizedMessage
        }

        val apiError = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Malformed JSON request",
            message = detailedMessage,
            originalMessage = originalMessage,
            classPath = ex.classPath
        )
            .also { logError(it) }

        return ResponseEntity(apiError, HttpStatus.BAD_REQUEST)
    }

    /**
     * Обрабатывает исключения некорректных значений параметров.
     *
     * @param ex Исключение, возникающее при некорректных параметрах.
     * @return Ответ с описанием ошибок.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(
        ex: MethodArgumentTypeMismatchException
    ): ResponseEntity<ErrorResponse> {

        val fieldName = ex.name
        val requiredType = ex.requiredType?.simpleName
        val invalidValue = ex.value

        val message = "Некорректное значение параметра '$fieldName'. " +
                "Ожидался тип '$requiredType', получено значение '$invalidValue'."

        val apiError = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Invalid request parameter",
            message = message,
            classPath = ex.classPath
        )
            .also { logError(it) }

        return ResponseEntity(apiError, HttpStatus.BAD_REQUEST)
    }

    /**
     * Получает путь до класса вместе с его названием, в котором была ошибка
     */
    private val Throwable.classPath: String
        get() = this.stackTrace.firstOrNull()?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"

    /**
     * Логирует сформированную ApiError перед возвращением ответа
     *
     * @param error логируемая ошибка
     */
    private fun logError(error: ErrorResponse) {
        println("Unexpected exception at [${error.classPath}]: ${error.message}, ${error.originalMessage}")
    }

    /**
     * Логирует сформированную ApiError перед возвращением ответа
     *
     * @param error логируемая ошибка
     */
    private fun Logger.apiError(error: ErrorResponse) = logError(error)

    /**
     * Поиск статуса из аннотации к ошибке
     *
     * @receiver Exception проверяемая ошибка
     * @return Статус указанный в аннотацие
     */
    private fun Exception.resolveAnnotatedStatus(): HttpStatus? =
        this::class.findAnnotation<ResponseStatus>()?.value


    inline fun <reified T : Annotation> KAnnotatedElement.findAnnotation(): T? =
        @Suppress("UNCHECKED_CAST")
        annotations.firstOrNull { it is T } as T?
}
