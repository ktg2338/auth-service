package com.aifactory.authservice.config

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "jwt")
@Validated
data class JwtProperties(
    @field:NotBlank(message = "JWT secret is required")
    @field:Size(min = 32, message = "JWT secret must be at least 32 characters for security")
    val secret: String,

    @field:Positive(message = "Access token expiration must be positive")
    val accessTokenExpiration: Long,

    @field:Positive(message = "Refresh token expiration must be positive")
    val refreshTokenExpiration: Long,

    @field:NotBlank(message = "JWT issuer is required")
    val issuer: String
)