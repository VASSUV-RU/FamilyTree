package ru.vassuv.familytree.api.controller

import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.vassuv.familytree.api.dto.request.SwitchActiveFamilyRequest
import ru.vassuv.familytree.api.dto.response.session.AuthTokensResponse
import ru.vassuv.familytree.api.security.UserPrincipal
import ru.vassuv.familytree.service.auth.TokenService

@RestController
@RequestMapping("/me")
@Validated
class MeController(
    private val tokenService: TokenService,
) {
    @PostMapping("/active-family")
    @PreAuthorize("isAuthenticated()")
    fun switchActiveFamily(
        @AuthenticationPrincipal principal: UserPrincipal?,
        @Valid @RequestBody req: SwitchActiveFamilyRequest,
    ): AuthTokensResponse {
        val p = principal ?: throw ru.vassuv.familytree.config.exception.UnauthorizeException()
        val tokens = tokenService.switchActiveFamily(userId = p.userId, currentJti = p.jti, familyId = req.familyId!!)
        return AuthTokensResponse(accessToken = tokens.accessToken, refreshToken = tokens.refreshToken)
    }
}

