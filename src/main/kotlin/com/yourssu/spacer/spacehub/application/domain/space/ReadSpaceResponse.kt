package com.yourssu.spacer.spacehub.application.domain.space

import com.yourssu.spacer.spacehub.business.domain.organization.OrganizationDto
import com.yourssu.spacer.spacehub.business.domain.space.SpaceDto
import java.time.LocalTime

data class ReadSpaceResponse(
    val organization: OrganizationItemResponse,
    val space: SpaceItemResponse,
) {
    companion object {
        fun from(spaceDto: SpaceDto): ReadSpaceResponse {
            val organization = OrganizationItemResponse.from(spaceDto.organization)
            val space = SpaceItemResponse.from(spaceDto)

            return ReadSpaceResponse(organization, space)
        }
    }

    data class OrganizationItemResponse(

        val id: Long,
        val name: String,
        val logoImageUrl: String,
        val description: String?,
        val hashtags: List<String> = emptyList(),
    ) {
        companion object {
            fun from(dto: OrganizationDto) = OrganizationItemResponse(
                id = dto.id,
                name = dto.name,
                logoImageUrl = dto.logoImageUrl,
                description = dto.description,
                hashtags = dto.hashtags,
            )
        }
    }

    data class SpaceItemResponse(

        val id: Long,
        val name: String,
        val spaceImageUrl: String,
        val location: String,
        val openingTime: LocalTime,
        val closingTime: LocalTime,
        val capacity: Int,
    ) {
        companion object {
            fun from(dto: SpaceDto) = SpaceItemResponse(
                id = dto.id,
                name = dto.name,
                spaceImageUrl = dto.spaceImageUrl ?: dto.organization.logoImageUrl,
                location = dto.location,
                openingTime = dto.openingTime,
                closingTime = dto.closingTime,
                capacity = dto.capacity,
            )
        }
    }
}
