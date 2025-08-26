package com.yourssu.spacer.spacehub.storage.domain.space

import org.springframework.data.jpa.repository.JpaRepository

interface JpaSpaceRepository : JpaRepository<SpaceEntity, Long> {

    fun findAllByOrganizationId(organizationId: Long): List<SpaceEntity>
}
