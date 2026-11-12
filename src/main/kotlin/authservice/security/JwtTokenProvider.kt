package com.aifactory.authservice.security

import com.aifactory.authservice.config.JwtProperties
import com.aifactory.authservice.dto.common.TokenDto
import com.aifactory.authservice.exception.InvalidTokenException
import com.aifactory.authservice.service.RedisService
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory

import org.springframework.stereotype.Component
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties,
    private val redisService: RedisService
) {

    private val logger = LoggerFactory.getLogger(JwtTokenProvider::class.java)
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())

    fun generateTokens(username: String): TokenDto {
        val accessToken = generateAccessToken(username)
        val refreshToken = generateRefreshToken(username)

        return TokenDto(
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessTokenExpiresIn = System.currentTimeMillis() + jwtProperties.accessTokenExpiration,
            refreshTokenExpiresIn = System.currentTimeMillis() + jwtProperties.refreshTokenExpiration
        )
    }

    fun generateAccessToken(username: String): String {
        val now = Date()
        val expirationDate = Date(now.time + jwtProperties.accessTokenExpiration)

        return Jwts.builder()
            .subject(username)
            .issuedAt(now)
            .expiration(expirationDate)
            .claim("type", "access")
            .signWith(secretKey)
            .compact()
    }

    fun generateRefreshToken(username: String): String {
        val now = Date()
        val expirationDate = Date(now.time + jwtProperties.refreshTokenExpiration)
        val tokenId = UUID.randomUUID().toString()

        // Redis에 refresh token 저장
        redisService.setValueWithExpiration(
            "refresh_token:$username:$tokenId",
            "",
            jwtProperties.refreshTokenExpiration,
            TimeUnit.MILLISECONDS
        )

        return Jwts.builder()
            .subject(username)
            .claim("type", "refresh")
            .claim("jti", tokenId)
            .issuedAt(now)
            .expiration(expirationDate)
            .signWith(secretKey)
            .compact()
    }

    fun validateAccessToken(token: String): Boolean {
        return try {
            val claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload

            // refresh token 등 다른 타입의 토큰으로 인증하는 것을 차단
            if (claims["type"] != "access") {
                logger.warn("Rejected non-access token used for authentication")
                return false
            }
            true
        } catch (e: JwtException) {
            logger.error("Invalid JWT token: ${e.message}")
            false
        } catch (e: IllegalArgumentException) {
            logger.error("JWT claims string is empty: ${e.message}")
            false
        }
    }

    fun validateRefreshToken(token: String): Boolean {
        return try {
            val claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload

            // Refresh token인 경우 Redis에서 유효성 확인
            if (claims["type"] == "refresh") {
                logger.info("JWT claims string is empty: ${claims["jti"]}")
                val username = claims.subject
                val jti = claims["jti"] as String
                return redisService.hasKey("refresh_token:$username:$jti")
            }
            false
        } catch (e: JwtException) {
            logger.error("Invalid JWT token: ${e.message}")
            false
        } catch (e: IllegalArgumentException) {
            logger.error("JWT claims string is empty: ${e.message}")
            false
        }
    }

    fun getUsernameFromToken(token: String): String {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload
                .subject
        } catch (e: JwtException) {
            logger.error("Failed to extract username from token: ${e.message}")
            throw InvalidTokenException("토큰에서 사용자 정보를 추출할 수 없습니다")
        } catch (e: IllegalArgumentException) {
            logger.error("Token is empty: ${e.message}")
            throw InvalidTokenException("토큰이 비어있습니다")
        }
    }

    fun getJtiFromRefreshToken(token: String): String? {
        return try {
            val claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
            claims.payload["jti"] as String
        } catch (e: Exception) {
            null
        }
    }

    fun revokeRefreshToken(username: String, refreshToken: String) {
        val jti = getJtiFromRefreshToken(refreshToken)
        if (jti != null) {
            redisService.deleteValue("refresh_token:$username:$jti")
        }
    }

    fun revokeAllRefreshTokens(username: String) {
        redisService.deleteAllByUsername(username)
    }
}