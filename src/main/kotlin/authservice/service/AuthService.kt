package com.aifactory.authservice.service

import com.aifactory.authservice.dto.common.TokenDto
import com.aifactory.authservice.dto.request.LoginRequest
import com.aifactory.authservice.dto.request.SignUpRequest
import com.aifactory.authservice.entity.User
import com.aifactory.authservice.entity.UserRole
import com.aifactory.authservice.exception.DuplicateEmailException
import com.aifactory.authservice.exception.DuplicateUsernameException
import com.aifactory.authservice.exception.InvalidTokenException
import com.aifactory.authservice.repository.UserRepository
import com.aifactory.authservice.security.JwtTokenProvider
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
@Transactional
class AuthService(
    private val userDetailsService: UserDetailsService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userRepository: UserRepository
) {

    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    fun signUp(request: SignUpRequest) {
        // 중복 체크
        if (userRepository.findByUsername(request.username) != null) {
            throw DuplicateUsernameException("이미 사용중인 아이디입니다: ${request.username}")
        }

        if (userRepository.findByEmail(request.email) != null) {
            throw DuplicateEmailException("이미 사용중인 이메일입니다: ${request.email}")
        }

        try {
            val user = User(
                username = request.username,
                password = passwordEncoder.encode(request.password),
                name = request.name,
                email = request.email,
                roles = mutableSetOf(UserRole.USER)
            )
            userRepository.save(user)
            logger.info("User registered successfully: ${request.username}")
        } catch (e: DataIntegrityViolationException) {
            logger.error("Data integrity violation during signup: ${e.message}")
            throw DuplicateUsernameException("회원가입 중 오류가 발생했습니다. 입력 정보를 확인해주세요")
        } catch (e: Exception) {
            logger.error("Unexpected error during signup: ${e.message}", e)
            throw RuntimeException("회원가입 중 오류가 발생했습니다")
        }
    }

    fun login(loginRequest: LoginRequest): TokenDto {
        val userDetails = userDetailsService.loadUserByUsername(loginRequest.username)
        if (!passwordEncoder.matches(loginRequest.password, userDetails.password)) {
            throw BadCredentialsException("Invalid credentials")
        }

        // 기존 refresh token 무효화 - Redis 실패해도 로그인 진행
        try {
            jwtTokenProvider.revokeAllRefreshTokens(loginRequest.username)
        } catch (e: Exception) {
            logger.warn("Failed to revoke existing refresh tokens for user ${loginRequest.username}: ${e.message}")
            // 로그인 진행
        }

        return jwtTokenProvider.generateTokens(loginRequest.username)
    }

    fun refresh(refreshToken: String): TokenDto {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw InvalidTokenException("Invalid or expired refresh token")
        }
        val username = jwtTokenProvider.getUsernameFromToken(refreshToken)

        // 기존 refresh token 무효화 - Redis 실패해도 진행
        try {
            jwtTokenProvider.revokeAllRefreshTokens(username)
        } catch (e: Exception) {
            logger.warn("Failed to revoke existing refresh tokens for user $username: ${e.message}")
        }

        return jwtTokenProvider.generateTokens(username)
    }

    fun logout(username: String, refreshToken: String?) {
        refreshToken?.let {
            try {
                jwtTokenProvider.revokeRefreshToken(username, it)
                logger.info("User logged out successfully: $username")
            } catch (e: Exception) {
                logger.error("Failed to revoke refresh token during logout for user $username: ${e.message}", e)
                // 로그아웃은 계속 진행 (클라이언트에서 쿠키 삭제)
            }
        }
    }
}