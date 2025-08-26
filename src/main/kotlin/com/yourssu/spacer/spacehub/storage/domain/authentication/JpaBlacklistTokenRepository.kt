package com.yourssu.spacer.spacehub.storage.domain.authentication

import org.springframework.data.jpa.repository.JpaRepository

interface JpaBlacklistTokenRepository : JpaRepository<BlacklistTokenEntity, Long> {

    fun existsByOrganizationIdAndToken(organizationId: Long, token: String): Boolean
}
