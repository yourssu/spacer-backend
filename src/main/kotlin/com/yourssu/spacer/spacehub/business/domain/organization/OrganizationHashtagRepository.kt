package com.yourssu.spacer.spacehub.business.domain.organization

interface OrganizationHashtagRepository {

    fun save(organizationHashtag: OrganizationHashtag): OrganizationHashtag

    fun findByOrganizationIdAndHashtagId(
        organizationId: Long,
        hashtagId: Long
    ): OrganizationHashtag?

    fun findAllByOrganizationId(organizationId: Long): List<OrganizationHashtag>

    fun deleteAllByOrganizationId(organizationId: Long)
}
