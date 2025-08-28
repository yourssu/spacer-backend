package com.yourssu.spacer.spacehub.implement.domain.authentication

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class BlacklistTokenReader(
    private val blacklistTokenRepository: BlacklistTokenRepository,
) {

    fun isBlacklisted(organizationId: Long, targetToken: String): Boolean {
        return blacklistTokenRepository.existsByOrganizationIdAndToken(organizationId, targetToken)
    }
}
