package ru.vassuv.familytree.api.auth

import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ru.vassuv.familytree.api.controller.AuthController
import ru.vassuv.familytree.service.auth.TokenService
import ru.vassuv.familytree.service.auth.AccessValidator
import ru.vassuv.familytree.service.model.AuthTokens

@WebMvcTest(AuthController::class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    lateinit var mvc: MockMvc

    @MockitoBean
    lateinit var tokenService: TokenService

    @MockitoBean
    lateinit var accessValidator: AccessValidator

    @Test
    fun refresh_ok_with_body_returns_tokens() {
        whenever(tokenService.refresh("R1")).thenReturn(AuthTokens(accessToken = "acc", refreshToken = "R1"))

        mvc.perform(
            post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"R1\"}")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value("acc"))
            .andExpect(jsonPath("$.refreshToken").value("R1"))
    }

    @Test
    fun refresh_missing_body_yields_bad_request() {
        mvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest)
    }
}
