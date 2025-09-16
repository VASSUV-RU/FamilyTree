package ru.vassuv.familytree.config.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
class TooManyRequestsException(message: String? = null): RuntimeException(message)

fun tooManyRequestsError(message: String? = null): Nothing = throw TooManyRequestsException(message)

