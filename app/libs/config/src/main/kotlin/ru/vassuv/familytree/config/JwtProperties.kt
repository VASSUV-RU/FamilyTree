package ru.vassuv.familytree.config

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "auth.jwt")
data class JwtProperties(
    @field:NotBlank val issuer: String = "family-tree",
    @field:Positive val accessTtlSeconds: Long = 900,
    @field:Positive val refreshTtlSeconds: Long = 2592000,
    val publicKeyPem: String? = null,
    val privateKeyPem: String? = null,
)

