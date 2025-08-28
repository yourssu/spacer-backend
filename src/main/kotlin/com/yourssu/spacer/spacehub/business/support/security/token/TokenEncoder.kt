package com.yourssu.spacer.spacehub.business.support.security.token

import com.yourssu.spacer.spacehub.implement.domain.authentication.TokenType
import java.time.LocalDateTime

interface TokenEncoder {

    fun encode(
        issueTime: LocalDateTime,
        tokenType: TokenType,
        privateClaims: Map<String, Any>
    ): String
}
