package ru.vassuv.familytree.config.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class NotFoundException(message: String?): RuntimeException(message)

fun notFoundError(message: String? = null): Nothing = throw NotFoundException(message)

@ResponseStatus(HttpStatus.CONFLICT)
class ConflictException(message: String?): RuntimeException(message)

fun conflictError(message: String? = null): Nothing = throw ConflictException(message)

@ResponseStatus(HttpStatus.GONE)
class GoneException(message: String?): RuntimeException(message)

fun goneError(message: String? = null): Nothing = throw GoneException(message)

