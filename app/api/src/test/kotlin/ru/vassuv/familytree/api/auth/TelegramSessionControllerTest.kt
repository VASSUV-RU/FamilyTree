package ru.vassuv.familytree.api.auth

import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ru.vassuv.familytree.api.controller.TelegramController
import ru.vassuv.familytree.service.auth.CreatedTelegramSession
import ru.vassuv.familytree.service.auth.TelegramSessionService

@WebMvcTest(TelegramController::class)
@TestPropertySource(
    properties = ["auth.telegram.bot-username=test_bot", "auth.telegram.session-ttl-seconds=300"]
)
class TelegramSessionControllerTest {

    @Autowired
    lateinit var mvc: MockMvc

    @MockitoBean
    lateinit var service: TelegramSessionService

    @Test
    fun create_session_ok() {
        Mockito.`when`(service.createSession(null, 300)).thenReturn(CreatedTelegramSession("Sabc", 300))

        mvc.perform(
            post("/auth/telegram/session").contentType(MediaType.APPLICATION_JSON).content("{}")
        ).andExpect(status().isOk).andExpect(jsonPath("$.sid").value("Sabc"))
            .andExpect(jsonPath("$.deeplinkUrl").value("https://t.me/test_bot?start=Sabc"))
            .andExpect(jsonPath("$.expiresIn").value(300))
    }
}
