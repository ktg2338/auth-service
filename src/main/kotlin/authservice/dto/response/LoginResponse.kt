package com.aifactory.authservice.dto.response

data class LoginResponse(
    val accessTokenExpiresIn: Long,
    val refreshTokenExpiresIn: Long,
    val username: String,
    val csrfToken: String
)