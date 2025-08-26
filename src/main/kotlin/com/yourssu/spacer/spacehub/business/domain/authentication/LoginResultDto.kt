package com.yourssu.spacer.spacehub.business.domain.authentication

data class LoginResultDto(
    val id: Long,
    val name: String,
    val tokens: TokenDto,
)
