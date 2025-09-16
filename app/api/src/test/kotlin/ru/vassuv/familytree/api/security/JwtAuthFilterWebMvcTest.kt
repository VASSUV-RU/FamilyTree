package ru.vassuv.familytree.api.security

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ru.vassuv.familytree.api.controller.HealthController
import ru.vassuv.familytree.service.auth.AccessValidator
import ru.vassuv.familytree.service.auth.ValidatedSession

@WebMvcTest(HealthController::class)
@Import(SecurityConfig::class)
class JwtAuthFilterWebMvcTest {

  @Autowired
  lateinit var mvc: MockMvc
  @MockitoBean
  lateinit var accessValidator: AccessValidator

  @Test
  fun `unauthorized without bearer header`() {
    mvc.perform(get("/health")).andExpect(status().isUnauthorized)
  }

  @Test
  fun `authorized with bearer header`() {
    whenever(accessValidator.validate(any())).thenReturn(
      ValidatedSession(
        userId = 1,
        jti = "j",
        activeFamilyId = null,
        scopes = null
      )
    )
    mvc.perform(get("/health").header("Authorization", "Bearer x.y.z")).andExpect(status().isOk)
  }
}

