package com.yourssu.spacer.spacehub.storage.domain.space

import com.yourssu.spacer.spacehub.implement.domain.space.Space
import com.yourssu.spacer.spacehub.implement.domain.space.SpaceRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class SpaceRepositoryImpl(

    private val jpaSpaceRepository: JpaSpaceRepository
) : SpaceRepository {

    override fun save(space: Space): Space {
        return jpaSpaceRepository.save(SpaceEntity.from(space)).toDomain()
    }

    override fun findById(spaceId: Long): Space? {
        return jpaSpaceRepository.findByIdOrNull(spaceId)?.toDomain()
    }

    override fun findAllByOrganizationId(organizationId: Long): List<Space> {
        return jpaSpaceRepository.findAllByOrganizationId(organizationId).map { it.toDomain() }
    }
}
