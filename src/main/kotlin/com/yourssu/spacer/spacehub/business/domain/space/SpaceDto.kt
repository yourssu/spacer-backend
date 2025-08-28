package com.yourssu.spacer.spacehub.business.domain.space

import com.yourssu.spacer.spacehub.business.domain.organization.OrganizationDto
import com.yourssu.spacer.spacehub.implement.domain.space.Space
import java.time.LocalTime

data class SpaceDto(

    val id: Long,
    val organization: OrganizationDto,
    val name: String,
    val location: String,
    val spaceImageUrl: String? = null,
    val openingTime: LocalTime,
    val closingTime: LocalTime,
    val capacity: Int,
) {

    companion object {
        fun from(space: Space): SpaceDto = SpaceDto(
            id = space.id!!,
            organization = OrganizationDto.from(space.organization),
            name = space.name,
            location = space.location,
            spaceImageUrl = space.spaceImageUrl,
            openingTime = space.getOpeningTime(),
            closingTime = space.getClosingTime(),
            capacity = space.getCapacityValue(),
        )
    }
}
