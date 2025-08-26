package com.yourssu.spacer.spacehub.storage.domain.organization

import com.yourssu.spacer.spacehub.business.domain.organization.OrganizationHashtag
import com.yourssu.spacer.spacehub.business.domain.organization.OrganizationHashtagRepository
import org.springframework.stereotype.Repository

@Repository
class OrganizationHashtagRepositoryImpl(
    private val jpaOrganizationHashtagRepository: JpaOrganizationHashtagRepository
) : OrganizationHashtagRepository {

    override fun save(organizationHashtag: OrganizationHashtag): OrganizationHashtag {
        return jpaOrganizationHashtagRepository.save(OrganizationHashtagEntity.from(organizationHashtag)).toDomain()
    }

    override fun findByOrganizationIdAndHashtagId(
        organizationId: Long,
        hashtagId: Long
    ): OrganizationHashtag? {
        return jpaOrganizationHashtagRepository.findByOrganizationIdAndHashtagId(organizationId, hashtagId)?.toDomain()
    }

    override fun findAllByOrganizationId(organizationId: Long): List<OrganizationHashtag> {
        return jpaOrganizationHashtagRepository.findAllByOrganizationId(organizationId).map { it.toDomain() }
    }

    override fun deleteAllByOrganizationId(organizationId: Long) {
        jpaOrganizationHashtagRepository.deleteAllByOrganizationId(organizationId)
    }
}
