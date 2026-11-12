package com.aifactory.authservice.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SignUpRequest(

    @field:NotBlank(message = "아이디는 필수입니다")
    @field:Size(min = 8, max = 30, message = "아이디는 최소 8자 이상이어야 합니다")
    val username: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Size(min = 8, max = 30, message = "비밀번호는 최소 8자 이상이어야 합니다")
    val password: String,

    @field:NotBlank(message = "이름은 필수입니다")
    @field:Size(min = 3, max = 30, message = "비밀번호는 최소 8자 이상이어야 합니다")
    val name: String,

    @field:Email(message = "유효한 이메일 주소를 입력해주세요")
    @field:NotBlank(message = "이메일은 필수입니다")
    val email: String

)
