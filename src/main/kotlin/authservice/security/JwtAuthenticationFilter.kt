package com.aifactory.authservice.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userDetailsService: UserDetailsService
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    // JWT 필터를 적용하지 않을 경로들
//    private val excludeUrlPatterns = listOf(
//        "/auth/login",
//        "/auth/signup",
//        "/actuator/health/",
//        "/docs/",
//        "/swagger-resources/",
//        "/webjars/"
//    )

//    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
//        val path = request.requestURI
//        return excludeUrlPatterns.any { pattern ->
//            path.startsWith(pattern)
//        }
//    }



    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val token = extractTokenFromRequest(request)

            if (token != null && jwtTokenProvider.validateAccessToken(token)) {
                val username = jwtTokenProvider.getUsernameFromToken(token)
                val userDetails = userDetailsService.loadUserByUsername(username)

                val authentication = UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.authorities
                )

                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (e: Exception) {
            // 토큰 검증 실패 시 로그만 남기고 필터 체인 계속 진행
            // SecurityContext에 인증 정보가 없으면 Spring Security가 자동으로 401 처리
            logger.error("JWT authentication failed: ${e.message}")
            SecurityContextHolder.clearContext()
        }

        filterChain.doFilter(request, response)
    }

    private fun extractTokenFromRequest(request: HttpServletRequest): String? {

        // 쿠키에서 accessToken 확인
        val cookies = request.cookies
        if (cookies != null) {
            for (cookie in cookies) {
                if (cookie.name == "accessToken") {
                    return cookie.value
                }
            }
        }
        return null
    }
}