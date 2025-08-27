package com.yourssu.spacer.spacehub.implement.domain.authentication

interface BlacklistTokenRepository {

    fun saveAll(blacklistTokens: List<BlacklistToken>): List<BlacklistToken>
    fun existsByOrganizationIdAndToken(organizationId: Long, targetToken: String): Boolean
}
