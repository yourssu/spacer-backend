package com.yourssu.spacer.spacehub.business.domain.space

interface SpaceRepository {

    fun save(space: Space): Space
    fun findById(spaceId: Long): Space?
    fun findAllByOrganizationId(organizationId: Long): List<Space>
}
