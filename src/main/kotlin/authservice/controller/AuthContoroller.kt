package com.aifactory.authservice.controller

import com.aifactory.authservice.dto.request.LoginRequest
import com.aifactory.authservice.dto.request.SignUpRequest
import com.aifactory.authservice.service.AuthService
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.time.Duration

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {
    @Value("\${server.servlet.session.cookie.domain}")
    private lateinit var cookieDomain: String

    @Value("\${jwt.access-token-expiration}")
    private var accessTokenExpiration: Long = 0

    @Value("\${jwt.refresh-token-expiration}")
    private var refreshTokenExpiration: Long = 0

    @PostMapping("/signup")
    fun signUp(@Valid @RequestBody request: SignUpRequest): ResponseEntity<Map<String, Any>> {
        authService.signUp(request)
        return ResponseEntity.ok(mapOf("message" to "sign up successfully"))
    }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody loginRequest: LoginRequest,
        response: HttpServletResponse
    ): ResponseEntity<Map<String, Any>> {
        val tokenDto = authService.login(loginRequest)

        setTokenCookies(
            response,
            tokenDto.accessToken,
            tokenDto.refreshToken
        )

        return ResponseEntity.ok(mapOf(
            "message" to "login successfully"
        ))
    }

    @PostMapping("/refresh")
    fun refresh(
        @CookieValue("refreshToken") refreshToken: String,
        response: HttpServletResponse
    ): ResponseEntity<Map<String, Any>> {
        val tokenDto = authService.refresh(refreshToken)

        setTokenCookies(
            response,
            tokenDto.accessToken,
            tokenDto.refreshToken
        )

        return ResponseEntity.ok(mapOf(
            "message" to "refresh successfully"
        ))
    }

    @PostMapping("/logout")
    fun logout(
        @CookieValue("refreshToken", required = false) refreshToken: String?,
        authentication: Authentication,
        response: HttpServletResponse
    ): ResponseEntity<Map<String, String>> {
        authService.logout(authentication.name, refreshToken)

        setTokenCookies(response, clear = true)

        return ResponseEntity.ok(mapOf("message" to "Logout successfully"))
    }

    private fun setTokenCookies(
        response: HttpServletResponse,
        accessToken: String = "",
        refreshToken: String = "",
        clear: Boolean = false
    ) {
        val (accessMaxAge, refreshMaxAge) = if (clear) {
            Duration.ZERO to Duration.ZERO
        } else {
            Duration.ofMillis(accessTokenExpiration) to Duration.ofMillis(refreshTokenExpiration)
        }

        val accessTokenCookie = createCookie("accessToken", accessToken, accessMaxAge)
        val refreshTokenCookie = createCookie("refreshToken", refreshToken, refreshMaxAge)

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
    }

    private fun createCookie(name: String, value: String, maxAge: Duration): ResponseCookie =
        ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .domain(cookieDomain)
            .maxAge(maxAge)
            .sameSite("None")
            .build()
}