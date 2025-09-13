package ru.vassuv.familytree.api.auth

import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ru.vassuv.familytree.api.controller.TelegramWebhookController
import ru.vassuv.familytree.config.TelegramAuthProperties
import ru.vassuv.familytree.service.auth.TelegramWebhookService
import ru.vassuv.familytree.service.auth.WebhookConfirmResult

@WebMvcTest(TelegramWebhookController::class)
@EnableConfigurationProperties(TelegramAuthProperties::class)
@TestPropertySource(
    properties = ["auth.telegram.bot-username=test_bot", "auth.telegram.session-ttl-seconds=300", "auth.telegram.webhook-secret=secret123"]
)
class TelegramWebhookControllerTest {

    @Autowired
    lateinit var mvc: MockMvc

    @MockitoBean
    lateinit var service: TelegramWebhookService

    private fun updateJson(text: String): String = """
        {
          "update_id": 1,
          "message": {
            "message_id": 2,
            "text": "$text",
            "from": {"id": 12345, "is_bot": false, "username": "john"},
            "chat": {"id": 12345, "type": "private"}
          }
        }
    """.trimIndent()

    private fun updateJsonNoText(): String = """
        {
          "update_id": 1,
          "message": {
            "message_id": 2,
            "chat": {"id": 12345, "type": "private"}
          }
        }
    """.trimIndent()

    private fun updateJsonNoFrom(text: String): String = """
        {
          "update_id": 1,
          "message": {
            "message_id": 2,
            "text": "$text",
            "chat": {"id": 12345, "type": "private"}
          }
        }
    """.trimIndent()

    @Test
    fun webhook_ok_confirms_session() {
        whenever(service.confirmStart(any(), any())).thenReturn(WebhookConfirmResult(true, "ok"))

        mvc.perform(
            post("/auth/telegram/webhook").contentType(MediaType.APPLICATION_JSON)
                .header("X-Telegram-Bot-Api-Secret-Token", "secret123").content(updateJson("/start Sabc"))
        ).andExpect(status().isOk).andExpect(jsonPath("$.ok").value(true))
    }

    @Test
    fun webhook_unauthorized_on_bad_secret() {
        whenever(service.validateSecretOrThrow(eq("bad"), eq("secret123")))
            .thenAnswer { throw ru.vassuv.familytree.config.exception.UnauthorizeException() }

        mvc.perform(
            post("/auth/telegram/webhook").contentType(MediaType.APPLICATION_JSON)
                .header("X-Telegram-Bot-Api-Secret-Token", "bad").content(updateJson("/start Sabc"))
        ).andExpect(status().isUnauthorized)

        // confirmStart should not be called
        Mockito.verify(service, Mockito.never()).confirmStart(any(), any())
    }

    @Test
    fun webhook_idempotent_already_ready() {
        Mockito.`when`(service.confirmStart(eq("Sabc"), any()))
            .thenReturn(WebhookConfirmResult(true, "Already confirmed"))

        mvc.perform(
            post("/auth/telegram/webhook").contentType(MediaType.APPLICATION_JSON)
                .header("X-Telegram-Bot-Api-Secret-Token", "secret123").content(updateJson("/start Sabc"))
        ).andExpect(status().isOk).andExpect(jsonPath("$.ok").value(true))
    }

    @Test
    fun webhook_non_start_message_returns_ok_without_calling_service() {
        mvc.perform(
            post("/auth/telegram/webhook").contentType(MediaType.APPLICATION_JSON)
                .header("X-Telegram-Bot-Api-Secret-Token", "secret123").content(updateJson("hello"))
        ).andExpect(status().isOk).andExpect(jsonPath("$.ok").value(true))

        // confirmStart should not be called for non-start message
        Mockito.verify(service, Mockito.never()).confirmStart(any(), any())
    }

    @Test
    fun webhook_no_text_or_from_returns_ok() {
        mvc.perform(
            post("/auth/telegram/webhook").contentType(MediaType.APPLICATION_JSON)
                .header("X-Telegram-Bot-Api-Secret-Token", "secret123").content(updateJsonNoText())
        ).andExpect(status().isOk).andExpect(jsonPath("$.ok").value(true))

        mvc.perform(
            post("/auth/telegram/webhook").contentType(MediaType.APPLICATION_JSON)
                .header("X-Telegram-Bot-Api-Secret-Token", "secret123").content(updateJsonNoFrom("/start Sabc"))
        ).andExpect(status().isOk).andExpect(jsonPath("$.ok").value(true))

        // confirmStart should not be called
        Mockito.verify(service, Mockito.never()).confirmStart(any(), any())
    }

    @Test
    fun webhook_start_with_bot_suffix_parsed() {
        whenever(service.confirmStart(eq("Sabc"), any())).thenReturn(WebhookConfirmResult(true, "ok"))

        mvc.perform(
            post("/auth/telegram/webhook").contentType(MediaType.APPLICATION_JSON)
                .header("X-Telegram-Bot-Api-Secret-Token", "secret123").content(updateJson("/start@test_bot Sabc"))
        ).andExpect(status().isOk).andExpect(jsonPath("$.ok").value(true))
    }
}
