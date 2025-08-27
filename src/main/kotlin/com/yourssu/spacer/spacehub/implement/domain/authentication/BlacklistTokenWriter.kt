package com.yourssu.spacer.spacehub.implement.domain.authentication

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class BlacklistTokenWriter(
    private val blacklistTokenRepository: BlacklistTokenRepository,
) {

    fun register(organizationId: Long, accessToken: String, refreshToken: String) {
        val blacklistTokens: MutableList<BlacklistToken> = mutableListOf()
        blacklistTokens.add(
            BlacklistToken(
                organizationId = organizationId,
                tokenType = TokenType.ACCESS,
                token = accessToken
            )
        )
        blacklistTokens.add(
            BlacklistToken(
                organizationId = organizationId,
                tokenType = TokenType.REFRESH,
                token = refreshToken
            )
        )

        blacklistTokenRepository.saveAll(blacklistTokens)
    }
}
