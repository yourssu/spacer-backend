package com.yourssu.spacer.spacehub.implement.domain.space

import com.yourssu.spacer.spacehub.implement.support.exception.SpaceNotFoundException
import com.yourssu.spacer.spacehub.implement.domain.organization.Organization
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class SpaceReader(
    private val spaceRepository: SpaceRepository,
) {

    fun getById(spaceId: Long): Space {
        return spaceRepository.findById(spaceId) ?: throw SpaceNotFoundException("지정한 공간을 찾을 수 없습니다.")
    }

    fun readAllByOrganization(organization: Organization): List<Space> {
        return spaceRepository.findAllByOrganizationId(organization.id!!)
    }
}
