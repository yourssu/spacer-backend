package com.yourssu.spacer.spacehub.business.support.security.token

import com.yourssu.spacer.spacehub.implement.domain.authentication.TokenType
import io.jsonwebtoken.Claims

interface TokenDecoder {

    fun decode(tokenType: TokenType, token: String): Claims?
}
