package com.yourssu.spacer.spacehub.application.domain.authentication

import com.yourssu.spacer.spacehub.business.domain.authentication.TokenDto

data class TokenRefreshResponse(

    val accessToken: String,
    val refreshToken: String,
) {

    companion object {
        fun from(tokenDto: TokenDto) = TokenRefreshResponse(
            accessToken = tokenDto.accessToken,
            refreshToken = tokenDto.refreshToken,
        )
    }
}
