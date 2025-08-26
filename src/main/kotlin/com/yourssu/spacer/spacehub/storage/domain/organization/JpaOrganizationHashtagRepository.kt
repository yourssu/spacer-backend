package com.yourssu.spacer.spacehub.storage.domain.organization

import org.springframework.data.jpa.repository.JpaRepository

interface JpaOrganizationHashtagRepository : JpaRepository<OrganizationHashtagEntity, Long> {
    fun findByOrganizationIdAndHashtagId(
        organizationId: Long,
        hashtagId: Long
    ): OrganizationHashtagEntity?

    fun findAllByOrganizationId(organizationId: Long): List<OrganizationHashtagEntity>

    fun deleteAllByOrganizationId(organizationId: Long)
}
