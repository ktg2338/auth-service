package com.aifactory.authservice.handler

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component
import java.net.URLEncoder

@Component
class OAuth2FailureHandler : SimpleUrlAuthenticationFailureHandler() {
    private val logger = LoggerFactory.getLogger(OAuth2FailureHandler::class.java)
    private val objectMapper = jacksonObjectMapper()

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        val errorMessage = when (exception) {
            is OAuth2AuthenticationException -> {
                val errorCode = exception.error?.errorCode ?: "unknown_error"
                val errorDescription = exception.error?.description ?: exception.message
                
                logger.error("OAuth2 authentication failed - Code: $errorCode, Description: $errorDescription")
                logger.error("Request URI: ${request.requestURI}")
                logger.error("Query String: ${request.queryString}")
                logger.error("Remote Address: ${request.remoteAddr}")
                logger.error("Headers: ${request.headerNames.toList().associateWith { request.getHeader(it) }}")
                
                when {
                    errorCode == "user_not_found" && exception.message?.contains(":") == true -> {
                        val parts = exception.message?.split(":")
                        val email = parts?.getOrNull(1) ?: ""
                        val name = parts?.getOrNull(2) ?: ""
                        val imageUrl = parts?.getOrNull(3) ?: ""
                        
                        handleSignupRequired(response, email, name, imageUrl)
                        return
                    }
                    errorCode == "oauth2_processing_error" -> "OAuth 처리 중 오류가 발생했습니다"
                    errorCode == "email_is_empty" -> "이메일 정보를 가져올 수 없습니다"
                    errorCode == "invalid_token_response" -> "토큰 응답이 올바르지 않습니다"
                    errorCode == "invalid_user_info_response" -> "사용자 정보 응답이 올바르지 않습니다"
                    else -> "인증에 실패했습니다: $errorDescription"
                }
            }
            else -> {
                logger.error("Authentication failed", exception)
                "인증에 실패했습니다: ${exception.message}"
            }
        }

        val redirectUrl = "/login?error=true&message=${URLEncoder.encode(errorMessage, "UTF-8")}"
        response.sendRedirect(redirectUrl)
    }

    private fun handleSignupRequired(
        response: HttpServletResponse,
        email: String,
        name: String,
        imageUrl: String
    ) {
        val jsonResponse = objectMapper.writeValueAsString(
            mapOf(
                "status" to "signup_required",
                "message" to "회원가입이 필요합니다",
                "email" to email,
                "name" to name,
                "imageUrl" to imageUrl
            )
        )
        
        response.contentType = "application/json;charset=UTF-8"
        response.status = HttpStatus.OK.value()
        response.writer.write(jsonResponse)
        response.writer.flush()
    }
}