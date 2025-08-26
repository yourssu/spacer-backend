package com.yourssu.spacer.spacehub.business.support.security.token

import io.jsonwebtoken.Claims

interface TokenDecoder {

    fun decode(tokenType: TokenType, token: String): Claims?
}
