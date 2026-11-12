package com.aifactory.authservice.handler

import com.aifactory.authservice.entity.OAuth2Subject
import com.aifactory.authservice.repository.OAuth2SubjectRepository
import com.aifactory.authservice.security.JwtTokenProvider
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class OAuth2SuccessHandler(
    private val jwtTokenProvider: JwtTokenProvider,
    private val oAuth2SubjectRepository: OAuth2SubjectRepository,
) : SimpleUrlAuthenticationSuccessHandler() {
    private val logger = LoggerFactory.getLogger(OAuth2SuccessHandler::class.java)
    @Value("\${server.servlet.session.cookie.domain}")
    private lateinit var cookieDomain: String

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        try {
            val oauth2User = authentication.principal as OAuth2User

            logger.info("OAuth2 authentication success: ${oauth2User.attributes}")

            val oAuth2AuthenticationToken = authentication as OAuth2AuthenticationToken
            val provider = oAuth2AuthenticationToken.authorizedClientRegistrationId

            val identifier = try {
                when(provider) {
                    "google" -> oauth2User.getAttribute<String>("sub")
                        ?: throw IllegalArgumentException("Google 'sub' attribute is missing")
                    "kakao" -> oauth2User.getAttribute<Any>("id")?.toString()
                        ?: throw IllegalArgumentException("Kakao 'id' attribute is missing")
                    else -> throw IllegalArgumentException("Unsupported provider: $provider")
                }
            } catch (e: Exception) {
                logger.error("Failed to extract identifier from OAuth2 user: ${e.message}", e)
                handleAuthenticationError(response, "사용자 정보를 가져올 수 없습니다")
                return
            }

            val oAuth2Subject = try {
                oAuth2SubjectRepository.findBySubject(identifier)
            } catch (e: Exception) {
                logger.error("Database error while finding OAuth2 subject: ${e.message}", e)
                handleAuthenticationError(response, "데이터베이스 오류가 발생했습니다")
                return
            }

            if (oAuth2Subject == null) {
                // 회원가입이 필요한 경우
                handleSignupRequired(response, oauth2User, provider)
            } else {
                // 기존 사용자 로그인 처리
                handleExistingUserLogin(response, oAuth2Subject)
            }
        } catch (e: Exception) {
            logger.error("Unexpected error during OAuth2 authentication: ${e.message}", e)
            handleAuthenticationError(response, "인증 중 오류가 발생했습니다")
        }
    }

    private fun handleSignupRequired(response: HttpServletResponse, oauth2User: OAuth2User, provider: String) {
        try {
            val userInfo = when(provider) {
                "google" -> {
                    mapOf(
                        "provider" to provider,
                        "identifier" to (oauth2User.getAttribute<String>("sub")
                            ?: throw IllegalStateException("Google 'sub' is null")),
                        "email" to oauth2User.getAttribute<String>("email"),
                        "name" to oauth2User.getAttribute<String>("name"),
                        "given_name" to oauth2User.getAttribute<String>("given_name"),
                        "picture" to oauth2User.getAttribute<String>("picture")
                    )
                }
                "kakao" -> {
                    val properties = oauth2User.getAttribute<Map<String, Any>>("properties")
                    val kakaoAccount = oauth2User.getAttribute<Map<String, Any>>("kakao_account")
                    //카카오는 이메일 받으려면 업체인증하고 개발자 콘솔에서 이메일 동의항목 체크
                    mapOf(
                        "provider" to provider,
                        "identifier" to (oauth2User.getAttribute<Any>("id")?.toString()
                            ?: throw IllegalStateException("Kakao 'id' is null")),
//                        "email" to kakaoAccount?.get("email")?.toString(),
                        "name" to properties?.get("nickname")?.toString(),
                        "picture" to properties?.get("profile_image")?.toString()
                    )
                }
                else -> {
                    logger.error("Unsupported OAuth2 provider in signup: $provider")
                    throw IllegalArgumentException("Unsupported provider: $provider")
                }
            }

            val jsonResponse = jacksonObjectMapper().writeValueAsString(
                mapOf(
                    "status" to "signup_required",
                    "message" to "회원가입이 필요합니다"
                ) + userInfo
            )
            response.contentType = "application/json;charset=UTF-8"
            response.status = HttpStatus.OK.value()
            response.writer.write(jsonResponse)
            logger.info("OAuth2 signup required for provider: $provider")

        } catch (e: IllegalStateException) {
            logger.error("Required OAuth2 attribute is missing: ${e.message}", e)
            handleAuthenticationError(response, "OAuth 제공자로부터 필수 정보를 받지 못했습니다")
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid provider in signup: ${e.message}", e)
            handleAuthenticationError(response, "지원하지 않는 OAuth 제공자입니다")
        } catch (e: Exception) {
            logger.error("Failed to process OAuth2 signup information: ${e.message}", e)
            handleAuthenticationError(response, "회원가입 정보를 처리할 수 없습니다", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    private fun handleExistingUserLogin(response: HttpServletResponse, oAuth2Subject: OAuth2Subject) {
        try {
            // 기존 refresh token 무효화 - 실패해도 로그인 진행
            try {
                jwtTokenProvider.revokeAllRefreshTokens(oAuth2Subject.user.username)
            } catch (e: Exception) {
                logger.warn("Failed to revoke existing refresh tokens for OAuth2 user ${oAuth2Subject.user.username}: ${e.message}")
            }

            val tokenDto = jwtTokenProvider.generateTokens(oAuth2Subject.user.username)

            val accessTokenCookie = ResponseCookie.from("accessToken", tokenDto.accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .domain(cookieDomain)
                .maxAge(Duration.ofHours(1))
                .sameSite("None")
                .build()

            val refreshTokenCookie = ResponseCookie.from("refreshToken", tokenDto.refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .domain(cookieDomain)
                .maxAge(Duration.ofDays(7))
                .sameSite("None")
                .build()

            // 헤더에 쿠키 추가
            response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
            response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())

            val jsonResponse = jacksonObjectMapper().writeValueAsString(
                mapOf(
                    "message" to "login successfully",
                )
            )
            response.contentType = "application/json;charset=UTF-8"
            response.status = HttpStatus.OK.value()
            response.writer.write(jsonResponse)
            logger.info("OAuth2 login successful for user: ${oAuth2Subject.user.username}")

        } catch (e: com.aifactory.authservice.exception.RedisConnectionException) {
            logger.error("Redis connection error during OAuth2 login: ${e.message}", e)
            handleAuthenticationError(response, "일시적인 서비스 오류가 발생했습니다", HttpStatus.SERVICE_UNAVAILABLE)
        } catch (e: Exception) {
            logger.error("Unexpected error during OAuth2 login: ${e.message}", e)
            handleAuthenticationError(response, "로그인 처리 중 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    private fun handleAuthenticationError(
        response: HttpServletResponse,
        message: String,
        status: HttpStatus = HttpStatus.UNAUTHORIZED
    ) {
        val jsonResponse = jacksonObjectMapper().writeValueAsString(
            mapOf(
                "status" to "error",
                "message" to message
            )
        )
        response.contentType = "application/json;charset=UTF-8"
        response.status = status.value()
        response.writer.write(jsonResponse)
    }
}