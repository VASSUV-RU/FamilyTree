package ru.vassuv.familytree.api.mapper

import ru.vassuv.familytree.api.dto.response.CreateTelegramSessionResponse
import ru.vassuv.familytree.api.dto.response.session.AwaitTelegramSessionResponse
import ru.vassuv.familytree.api.dto.response.session.AuthTokensResponse
import ru.vassuv.familytree.service.auth.PollDelivery
import ru.vassuv.familytree.service.model.CreatedTelegramSession

class TelegramAuthMapper {

    fun toCreateSessionResponse(session: CreatedTelegramSession, botUsername: String): CreateTelegramSessionResponse {
        require(botUsername.isNotBlank()) { "botUsername is blank" }
        return CreateTelegramSessionResponse(
            sid = session.sid,
            deeplinkUrl = "https://t.me/$botUsername?start=${session.sid}",
            expiresIn = session.expiresIn,
        )
    }

    fun mapPollResult(delivery: PollDelivery): AwaitTelegramSessionResponse {
        return when (delivery) {
            is PollDelivery.Pending -> AwaitTelegramSessionResponse(status = "pending", auth = null)
            is PollDelivery.Ready -> {
                val access = delivery.auth.accessToken
                val refresh = delivery.auth.refreshToken
                AwaitTelegramSessionResponse(status = "ready", auth = AuthTokensResponse(accessToken = access, refreshToken = refresh))
            }
        }
    }
}
