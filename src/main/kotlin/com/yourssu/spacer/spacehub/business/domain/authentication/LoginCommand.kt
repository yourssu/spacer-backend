package com.yourssu.spacer.spacehub.business.domain.authentication

import java.time.LocalDateTime

data class LoginCommand(
    val requestTime: LocalDateTime,
    val email: String,
    val password: String,
)
