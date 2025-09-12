package ru.vassuv.familytree.api.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class UnauthorizeException: Exception("")

fun unauthorizeError(): Nothing = throw UnauthorizeException()