package com.yourssu.spacer.spacehub.implement.domain.space

import com.yourssu.spacer.spacehub.business.domain.space.CreateSpaceCommand
import com.yourssu.spacer.spacehub.implement.domain.organization.Organization
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class SpaceWriter(
    private val spaceRepository: SpaceRepository,
) {

    fun write(
        command: CreateSpaceCommand,
        organization: Organization,
        spaceImageUrl: String?,
    ): Space {
        val toSave = Space(
            organization = organization,
            name = command.name,
            location = command.location,
            spaceImageUrl = spaceImageUrl,
            operatingTime = SpaceOperatingTime(command.openingTime, command.closingTime),
            capacity = Capacity(command.capacity),
        )

        return spaceRepository.save(toSave)
    }

    fun update(updatedSpace: Space) = spaceRepository.save(updatedSpace)
}
