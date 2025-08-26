package com.yourssu.spacer.spacehub.application.domain.authentication

import com.yourssu.spacer.spacehub.business.domain.authentication.LoginResultDto

data class LoginResponse(

    val id: Long,
    val name: String,
    val accessToken: String,
    val refreshToken: String,
) {

    companion object {
        fun from(loginResultDto: LoginResultDto) = LoginResponse(
            id = loginResultDto.id,
            name = loginResultDto.name,
            accessToken = loginResultDto.tokens.accessToken,
            refreshToken = loginResultDto.tokens.refreshToken,
        )
    }
}
