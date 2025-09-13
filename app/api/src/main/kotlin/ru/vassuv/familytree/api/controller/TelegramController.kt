package ru.vassuv.familytree.api.controller

import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.vassuv.familytree.api.dto.request.CreateTelegramSessionRequest
import ru.vassuv.familytree.api.dto.response.CreateTelegramSessionResponse
import ru.vassuv.familytree.api.dto.request.telegram.TelegramUpdate
import ru.vassuv.familytree.api.mapper.TelegramAuthMapper
import ru.vassuv.familytree.api.dto.response.session.AwaitTelegramSessionResponse
import ru.vassuv.familytree.config.TelegramAuthProperties
import ru.vassuv.familytree.service.auth.TelegramService

@RestController
@RequestMapping("/auth/telegram")
@Validated
class TelegramController(
  private val service: TelegramService,
  private val props: TelegramAuthProperties,
) {
  private val mapper: TelegramAuthMapper = TelegramAuthMapper()

  @PostMapping("/session")
  fun createSession(
    @RequestBody(required = false) req: CreateTelegramSessionRequest
  ): CreateTelegramSessionResponse {
    val session = service.createSession(req.invitationId, props.sessionTtlSeconds)
    return mapper.toCreateSessionResponse(session, props.botUsername)
  }

  @GetMapping("/session/{sid}")
  fun awaitLoginSession(@PathVariable sid: String): AwaitTelegramSessionResponse =
    mapper.mapPollResult(service.awaitLoginSession(sid))

  @PostMapping("/webhook")
  fun handleWebhook(
    @RequestBody update: TelegramUpdate,
    @RequestHeader(name = "X-Telegram-Bot-Api-Secret-Token", required = false) secret: String?,
  ): Any {
    service.validateSecretOrThrow(secret, props.webhookSecret)

    val text = update.message?.text ?: return mapOf("ok" to true)
    val from = update.message.from ?: return mapOf("ok" to true)

    val sid = service.parseStartSid(text) ?: return mapOf("ok" to true)
    val tg = TelegramService.TelegramUserInfo(
      id = from.id,
      username = from.username,
      firstName = from.first_name,
      lastName = from.last_name,
    )
    val result = service.confirmStart(sid, tg)
    return mapOf("ok" to result.ok, "message" to result.message)
  }
}
