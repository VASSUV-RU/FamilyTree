package ru.vassuv.familytree.service.invite

/**
 * Сервис приглашений: принимает инвайт и возвращает id активной семьи,
 * к которой было привязано членство пользователя.
 *
 * Ошибки:
 *  - NotFoundException — инвайт не найден/просрочен
 *  - ConflictException — уже принят/недействителен
 */
interface InvitationService {
    fun accept(invitationId: String, userTelegramId: Long): Long
}

