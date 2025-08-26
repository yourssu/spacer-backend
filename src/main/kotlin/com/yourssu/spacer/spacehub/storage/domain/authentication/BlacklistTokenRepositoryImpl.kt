package com.yourssu.spacer.spacehub.storage.domain.authentication

import com.yourssu.spacer.spacehub.business.domain.authentication.BlacklistToken
import com.yourssu.spacer.spacehub.business.domain.authentication.BlacklistTokenRepository
import org.springframework.stereotype.Repository

@Repository
class BlacklistTokenRepositoryImpl(
    private val jpaBlacklistTokenRepository: JpaBlacklistTokenRepository,
) : BlacklistTokenRepository {

    override fun saveAll(blacklistTokens: List<BlacklistToken>): List<BlacklistToken> {
        return jpaBlacklistTokenRepository.saveAll(
            blacklistTokens.map { BlacklistTokenEntity.from(it) }
        ).map { it.toDomain() }
    }

    override fun existsByOrganizationIdAndToken(organizationId: Long, targetToken: String): Boolean {
        return jpaBlacklistTokenRepository.existsByOrganizationIdAndToken(organizationId, targetToken)
    }
}
