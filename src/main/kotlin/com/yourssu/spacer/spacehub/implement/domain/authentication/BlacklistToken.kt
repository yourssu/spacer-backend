package com.yourssu.spacer.spacehub.implement.domain.authentication

import com.yourssu.spacer.spacehub.implement.support.exception.EmptyTokenException

class BlacklistToken(
    val id: Long? = null,
    val organizationId: Long,
    val tokenType: TokenType,
    val token: String,
) {

    init {
        if (token.isBlank()) {
            throw EmptyTokenException("토큰이 비어있습니다.")
        }
    }
}
