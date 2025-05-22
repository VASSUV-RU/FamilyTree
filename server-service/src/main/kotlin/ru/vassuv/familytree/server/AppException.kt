package ru.vassuv.familytree.server

sealed class AppException(message: String) : RuntimeException(message)

class ValidationException(message: String) : AppException(message)
class ConflictException(message: String) : AppException(message)
class NotFoundException(message: String) : AppException(message)
class UnauthorizedException(message: String) : AppException(message)
class ForbiddenException(message: String) : AppException(message)