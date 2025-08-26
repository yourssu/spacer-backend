package com.yourssu.spacer.spacehub.business.support.security.token

import java.time.LocalDateTime

interface TokenEncoder {

    fun encode(
        issueTime: LocalDateTime,
        tokenType: TokenType,
        privateClaims: Map<String, Any>
    ): String
}
