package ru.vassuv.familytree.api.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import ru.vassuv.familytree.config.exception.UnauthorizeException
import ru.vassuv.familytree.service.auth.AccessValidator

@Component
class JwtAuthFilter(
    private val validator: AccessValidator,
) : OncePerRequestFilter() {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val auth = request.getHeader("Authorization")
        if (auth.isNullOrBlank() || !auth.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }
        val token = auth.removePrefix("Bearer ").trim()
        try {
            val session = validator.validate(token)
            val principal = UserPrincipal(userId = session.userId, jti = session.jti, activeFamilyId = session.activeFamilyId, scopes = session.scopes)
            val authentication = UsernamePasswordAuthenticationToken(principal, null, emptyList())
            SecurityContextHolder.getContext().authentication = authentication
            logger.debug("Authorized userId={} jti={}", session.userId, session.jti)
        } catch (e: UnauthorizeException) {
            SecurityContextHolder.clearContext()
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            return
        }
        filterChain.doFilter(request, response)
    }
}

