package com.yourssu.spacer.spacehub.application.domain.authentication

import jakarta.validation.constraints.NotBlank

data class WithdrawalRequest(

    @NotBlank(message = "refresh token이 입력되지 않았습니다.")
    val refreshToken: String,
)
