package ru.vassuv.familytree.api.auth

import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ru.vassuv.familytree.api.controller.TelegramController
import ru.vassuv.familytree.config.TelegramAuthProperties
import ru.vassuv.familytree.data.auth.pending.MarkUsedResult
import ru.vassuv.familytree.service.auth.PollDelivery
import ru.vassuv.familytree.service.model.AuthTokens
import ru.vassuv.familytree.service.auth.TelegramService
import ru.vassuv.familytree.service.model.CreatedTelegramSession

@WebMvcTest(TelegramController::class)
@EnableConfigurationProperties(TelegramAuthProperties::class)
@TestPropertySource(
  properties = ["auth.telegram.bot-username=test_bot", "auth.telegram.session-ttl-seconds=300", "auth.telegram.webhook-secret=secret123"]
)

class TelegramControllerTest {

  @Autowired
  lateinit var mvc: MockMvc

  @MockitoBean
  lateinit var service: TelegramService

  @Test
  fun create_session_ok() {
    whenever(
      service.createSession(null, 300)
    ).thenReturn(CreatedTelegramSession("Sabc", 300))

    val request = post("/auth/telegram/session").contentType(MediaType.APPLICATION_JSON).content("{}")

    mvc.perform(request).andExpect(status().isOk).andExpect(jsonPath("$.sid").value("Sabc"))
      .andExpect(jsonPath("$.deeplinkUrl").value("https://t.me/test_bot?start=Sabc"))
      .andExpect(jsonPath("$.expiresIn").value(300))
  }

  @Test
  fun create_session_with_invitationId_passed_to_service() {
    whenever(service.createSession("inv-1", 300)).thenReturn(CreatedTelegramSession("Sxyz", 300))

    val body = "{" + "\"invitationId\":\"inv-1\"" + "}"
    val request = post("/auth/telegram/session").contentType(MediaType.APPLICATION_JSON).content(body)

    mvc.perform(request).andExpect(status().isOk).andExpect(jsonPath("$.sid").value("Sxyz"))
      .andExpect(jsonPath("$.deeplinkUrl").value("https://t.me/test_bot?start=Sxyz"))
      .andExpect(jsonPath("$.expiresIn").value(300))
  }

  @Test
  fun poll_session_pending() {
    whenever(service.awaitLoginSession("Sabc")).thenReturn(PollDelivery.Pending)

    mvc.perform(get("/auth/telegram/session/Sabc")).andExpect(status().isOk)
      .andExpect(jsonPath("$.status").value("pending"))
  }

  @Test
  fun poll_session_ready_returns_auth_without_cookie() {
    whenever(service.awaitLoginSession("Sabc")).thenReturn(
      PollDelivery.Ready(tokens = AuthTokens(accessToken = "acc-1", refreshToken = "rid-1"))
    )

    mvc.perform(get("/auth/telegram/session/Sabc")).andExpect(status().isOk)
      .andExpect(jsonPath("$.status").value("ready"))
      .andExpect(jsonPath("$.auth.accessToken").value("acc-1"))
      .andExpect(jsonPath("$.auth.refreshToken").value("rid-1"))
      .andExpect(header().doesNotExist("Set-Cookie"))
  }

  @Test
  fun poll_session_not_found_returns_404() {
    whenever(service.awaitLoginSession("S404")).thenAnswer {
      throw ru.vassuv.familytree.config.exception.NotFoundException(
        null
      )
    }
    mvc.perform(get("/auth/telegram/session/S404")).andExpect(status().isNotFound)
  }

  @Test
  fun poll_session_used_returns_409() {
    whenever(service.awaitLoginSession("Sused")).thenAnswer {
      throw ru.vassuv.familytree.config.exception.ConflictException(
        null
      )
    }
    mvc.perform(get("/auth/telegram/session/Sused")).andExpect(status().isConflict)
  }

  @Test
  fun poll_session_expired_returns_410() {
    whenever(service.awaitLoginSession("Sgone")).thenAnswer {
      throw ru.vassuv.familytree.config.exception.GoneException(
        null
      )
    }
    mvc.perform(get("/auth/telegram/session/Sgone")).andExpect(status().isGone)
  }
}
