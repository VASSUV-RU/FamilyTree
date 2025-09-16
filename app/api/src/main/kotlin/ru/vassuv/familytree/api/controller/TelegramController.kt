package ru.vassuv.familytree.api.controller

import jakarta.servlet.http.HttpServletRequest
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
import ru.vassuv.familytree.service.rate.RateLimiter

@RestController
@RequestMapping("/auth/telegram")
@Validated
class TelegramController(
  private val service: TelegramService,
  private val tgBotMachine: TelegramBotMachine,
  private val props: TelegramAuthProperties,
  private val rateLimiter: RateLimiter,
) {
  private val mapper: TelegramMapper = TelegramMapper()

  @PostMapping("/session")
  fun createSession(
    @RequestBody(required = false) req: CreateTelegramSessionRequest?,
    request: HttpServletRequest,
  ): CreateTelegramSessionResponse {
    // Rate limit по IP: 5 запросов за 30 секунд
    val ip = request.getHeader("X-Forwarded-For")?.split(',')?.firstOrNull()?.trim()
      ?: request.remoteAddr
    rateLimiter.ensureAllowedOrThrow("rate:create:$ip", limit = 5, windowSeconds = 30)
    val session = service.createSession(req?.invitationId, props.sessionTtlSeconds)
    return mapper.toCreateSessionResponse(session, props.botUsername)
  }

  @GetMapping("/session/{sid}")
  fun awaitLoginSession(@PathVariable sid: String): AwaitTelegramSessionResponse =
    // TODO(ft-auth-04): На статусе READY выставлять refresh-cookie (HttpOnly, Secure, SameSite=Lax/Strict, TTL)
    //   Сейчас refreshToken возвращается в теле ответа для простоты. Нужно перенести его в cookie,
    //   а в ответе оставить только accessToken, либо весь auth — но cookie обязательно.
    //   После успешной установки cookie пометить sid как USED (сервис уже делает markUsed).
    mapper.mapPollResult(service.awaitLoginSession(sid))

  @PostMapping("/webhook")
  fun handleWebhook(
    @RequestBody update: TelegramUpdateRequest,
    @RequestHeader(name = "X-Telegram-Bot-Api-Secret-Token", required = false) secret: String?,
  ): Any {
    service.validateSecretOrThrow(secret, props.webhookSecret)
    // Дедупликация по update_id (15 минут)
    val updKey = "tg:update:${update.update_id}"
    val firstTime = rateLimiter.tryAcquireUnique(updKey, ttlSeconds = 15 * 60)
    if (!firstTime) return mapOf("ok" to true)

    // Rate limit по пользователю (10/мин) и по sid (3/мин)
    val tgUserId = update.message?.from?.id ?: update.callback_query?.from?.id
    tgUserId?.let { rateLimiter.ensureAllowedOrThrow("rate:webhook:uid:$it", limit = 10, windowSeconds = 60) }
    val text = update.message?.text
    val sid = text?.let { service.parseStartSid(it) }
    sid?.let { rateLimiter.ensureAllowedOrThrow("rate:webhook:sid:$it", limit = 3, windowSeconds = 60) }
    return tgBotMachine.handle(update)
  }
}
