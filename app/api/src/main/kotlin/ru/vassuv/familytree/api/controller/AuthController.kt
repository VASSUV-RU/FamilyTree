package ru.vassuv.familytree.api.controller

import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.vassuv.familytree.api.dto.request.RefreshRequest
import ru.vassuv.familytree.api.dto.request.SwitchActiveFamilyRequest
import ru.vassuv.familytree.api.dto.response.session.AuthTokensResponse
import ru.vassuv.familytree.api.security.UserPrincipal
import ru.vassuv.familytree.service.auth.TokenService

@RestController
@RequestMapping("/auth")
@Validated
class AuthController(
    private val tokenService: TokenService,
) {
    @PostMapping("/refresh")
    fun refresh(
        @Valid @RequestBody req: RefreshRequest,
    ): AuthTokensResponse {
        val tokens = tokenService.refresh(req.refreshToken)
        return AuthTokensResponse(accessToken = tokens.accessToken, refreshToken = tokens.refreshToken)
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    fun logout(@AuthenticationPrincipal principal: UserPrincipal?): org.springframework.http.ResponseEntity<Void> {
        val p = principal ?: return org.springframework.http.ResponseEntity.status(401).build()
        tokenService.logout(p.jti)
        return org.springframework.http.ResponseEntity.noContent().build()
    }

}
