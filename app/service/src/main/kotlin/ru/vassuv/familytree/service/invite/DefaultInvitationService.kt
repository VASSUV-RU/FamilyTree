package ru.vassuv.familytree.service.invite

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.vassuv.familytree.config.exception.NotFoundException

@Service
class DefaultInvitationService : InvitationService {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun accept(invitationId: String, userTelegramId: Long): Long {
        // TODO(ft-auth-09): Реализовать логику проверки и принятия инвайта (БД + членство)
        // Сейчас выбрасываем NotFound, чтобы явно сигнализировать об отсутствующей реализации.
        logger.warn("Invitation acceptance not implemented: invitationId={}, userTelegramId={}", invitationId, userTelegramId)
        throw NotFoundException("Invitation not implemented")
    }
}

