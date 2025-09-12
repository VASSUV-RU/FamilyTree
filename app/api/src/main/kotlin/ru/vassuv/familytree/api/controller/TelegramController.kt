package ru.vassuv.familytree.api.controller

import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.vassuv.familytree.api.controller.dto.request.CreateTelegramSessionRequest
import ru.vassuv.familytree.api.controller.dto.response.CreateTelegramSessionResponse
import ru.vassuv.familytree.config.TelegramAuthProperties
import ru.vassuv.familytree.service.auth.TelegramSessionService

@RestController
@RequestMapping("/auth/telegram")
@Validated
class TelegramController(
    private val service: TelegramSessionService,
    private val props: TelegramAuthProperties,
) {
    @PostMapping("/session")
    fun createSession(@RequestBody(required = false) req: CreateTelegramSessionRequest?): CreateTelegramSessionResponse {
        if (props.botUsername.isBlank()) {
            error("botUsername is blank")
        }
        val created = service.createSession(req?.invitationId, props.sessionTtlSeconds)
        return CreateTelegramSessionResponse(
            sid = created.sid,
            deeplinkUrl = "https://t.me/${props.botUsername}?start=${created.sid}",
            expiresIn = created.expiresIn,
        )
    }
}
