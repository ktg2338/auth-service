package com.aifactory.authservice.dto.common

data class TokenDto(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresIn: Long,
    val refreshTokenExpiresIn: Long
)