package ru.vassuv.familytree.api.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import ru.vassuv.familytree.api.controller.dto.request.CreateTelegramSessionRequest
import ru.vassuv.familytree.api.controller.dto.response.CreateTelegramSessionResponse
import ru.vassuv.familytree.service.auth.TelegramSessionService

@RestController
@RequestMapping("/auth/telegram")
@Validated
class TelegramController(
    private val service: TelegramSessionService,
    @Value("\${auth.telegram.bot-username}")
    private val botUsername: String,
    @Value("\${auth.telegram.session-ttl-seconds:300}")
    private val sessionTtlSeconds: Long,
) {
    @PostMapping("/session")
    fun createSession(@RequestBody(required = false) req: CreateTelegramSessionRequest?): CreateTelegramSessionResponse {
        if (botUsername.isBlank()) {
            error("botUsername is blank")
        }
        val created = service.createSession(req?.invitationId, sessionTtlSeconds)
        return CreateTelegramSessionResponse(
            sid = created.sid,
            deeplinkUrl = "https://t.me/$botUsername?start=${created.sid}",
            expiresIn = created.expiresIn,
        )
    }
}

