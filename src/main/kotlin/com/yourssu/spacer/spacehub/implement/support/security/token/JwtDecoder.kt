package com.yourssu.spacer.spacehub.implement.support.security.token

import com.yourssu.spacer.spacehub.implement.support.exception.InvalidTokenException
import com.yourssu.spacer.spacehub.business.support.security.token.TokenDecoder
import com.yourssu.spacer.spacehub.implement.domain.authentication.TokenType
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.nio.charset.StandardCharsets
import org.springframework.stereotype.Component

@Component
class JwtDecoder(
    private val jwtProperties: JwtProperties
) : TokenDecoder {

    companion object {
        private const val BEARER_TOKEN_PREFIX = "Bearer "
    }

    override fun decode(tokenType: TokenType, token: String): Claims? {
        validateBearerToken(token)

        return parseToClaims(tokenType, token)
    }

    private fun validateBearerToken(token: String) {
        if (!token.startsWith(BEARER_TOKEN_PREFIX)) {
            throw InvalidTokenException("Bearer 타입이 아닙니다.")
        }
    }

    private fun parseToClaims(tokenType: TokenType, token: String): Claims? {
        val key: String = jwtProperties.findTokenKey(tokenType)

        return try {
            Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(key.toByteArray(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(findPureToken(token))
                .payload
        } catch (_: JwtException) {
            null
        }
    }

    private fun findPureToken(token: String): String {
        return token.substring(BEARER_TOKEN_PREFIX.length)
    }
}
