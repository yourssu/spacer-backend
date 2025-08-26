package com.yourssu.spacer.spacehub.application.domain.authentication

import com.yourssu.spacer.spacehub.business.domain.authentication.LoginCommand
import jakarta.validation.constraints.NotEmpty
import java.time.LocalDateTime

data class LoginRequest(

    @NotEmpty(message = "이메일이 입력되지 않았습니다.")
    val email: String,

    @NotEmpty(message = "비밀번호가 입력되지 않았습니다.")
    val password: String,
) {

    fun toCommand(): LoginCommand = LoginCommand(
        requestTime = LocalDateTime.now(),
        email = email,
        password = password,
    )
}
