package ru.vassuv.familytree.service.auth

import org.springframework.stereotype.Service
import ru.vassuv.familytree.service.model.AuthTokens
import java.util.UUID

interface TokenService {
    fun issueForTelegram(telegramId: Long, invitationId: String?): AuthTokens
}

@Service
class DefaultTokenService : TokenService {
    override fun issueForTelegram(telegramId: Long, invitationId: String?): AuthTokens {
        // Placeholder implementation; replace with real issuing (JWT + refresh store)
        val jti = UUID.randomUUID().toString()
        return AuthTokens(accessToken = "access-$jti", refreshToken = jti)
    }
}

