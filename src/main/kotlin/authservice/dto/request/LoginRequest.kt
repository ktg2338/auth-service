package com.aifactory.authservice.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LoginRequest(

    @field:NotBlank(message = "아이디는 필수입니다")
    @field:Size(min = 8, max = 30, message = "아이디는 최소 8자 이상이어야 합니다")
    val username: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Size(min = 8, max = 30, message = "비밀번호는 최소 8자 이상이어야 합니다")
    val password: String
)