package ru.vassuv.familytree.api.controller

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
import ru.vassuv.familytree.api.mapper.TelegramMapper
import ru.vassuv.familytree.api.dto.response.session.AwaitTelegramSessionResponse
import ru.vassuv.familytree.bot.telegram.TelegramBotMachine
import ru.vassuv.familytree.bot.telegram.webhook.dto.TelegramUpdateRequest
import ru.vassuv.familytree.config.TelegramAuthProperties
import ru.vassuv.familytree.service.auth.TelegramService

@RestController
@RequestMapping("/auth/telegram")
@Validated
class TelegramController(
  private val service: TelegramService,
  private val tgBotMachine: TelegramBotMachine,
  private val props: TelegramAuthProperties,
) {
  private val mapper: TelegramMapper = TelegramMapper()

  @PostMapping("/session")
  fun createSession(
    @RequestBody(required = false) req: CreateTelegramSessionRequest?
  ): CreateTelegramSessionResponse {
    val session = service.createSession(req?.invitationId, props.sessionTtlSeconds)
    return mapper.toCreateSessionResponse(session, props.botUsername)
  }

  @GetMapping("/session/{sid}")
  fun awaitLoginSession(@PathVariable sid: String): AwaitTelegramSessionResponse =
    mapper.mapPollResult(service.awaitLoginSession(sid))

  @PostMapping("/webhook")
  fun handleWebhook(
    @RequestBody update: TelegramUpdateRequest,
    @RequestHeader(name = "X-Telegram-Bot-Api-Secret-Token", required = false) secret: String?,
  ): Any {
    service.validateSecretOrThrow(secret, props.webhookSecret)
    return tgBotMachine.handle(update)
  }
}
