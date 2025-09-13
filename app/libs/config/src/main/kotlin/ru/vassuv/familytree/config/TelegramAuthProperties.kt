package ru.vassuv.familytree.config

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "auth.telegram")
data class TelegramAuthProperties(
    @field:NotBlank val botUsername: String,
    @field:Positive val sessionTtlSeconds: Long = 300,
    val webhookSecret: String = "",
)
