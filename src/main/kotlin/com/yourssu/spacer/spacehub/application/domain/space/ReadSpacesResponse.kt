package com.yourssu.spacer.spacehub.application.domain.space

import com.yourssu.spacer.spacehub.business.domain.organization.OrganizationDto
import com.yourssu.spacer.spacehub.business.domain.space.ReadSpacesResult
import com.yourssu.spacer.spacehub.business.domain.space.SpaceDto
import java.time.LocalTime

data class ReadSpacesResponse(
    val organization: ReadOrganizationItemResponse,
    val spaces: List<ReadSpaceItemResponse>
) {

    companion object {
        fun from(resultDto: ReadSpacesResult): ReadSpacesResponse {
            val organization = ReadOrganizationItemResponse.from(resultDto.organizationDto)
            val spaces = resultDto.spaceDtos.map {
                ReadSpaceItemResponse.from(it)
            }

            return ReadSpacesResponse(organization, spaces)
        }
    }
}

data class ReadOrganizationItemResponse(

    val id: Long,
    val name: String,
    val logoImageUrl: String,
    val description: String?,
    val hashtags: List<String> = emptyList(),
) {

    companion object {
        fun from(organizationDto: OrganizationDto) = ReadOrganizationItemResponse(
            id = organizationDto.id,
            name = organizationDto.name,
            logoImageUrl = organizationDto.logoImageUrl,
            description = organizationDto.description,
            hashtags = organizationDto.hashtags,
        )
    }
}

data class ReadSpaceItemResponse(

    val id: Long,
    val name: String,
    val location: String,
    val spaceImageUrl: String,
    val openingTime: LocalTime,
    val closingTime: LocalTime,
    val capacity: Int,
) {

    companion object {
        fun from(spaceDto: SpaceDto): ReadSpaceItemResponse {
            val spaceImageUrl: String = spaceDto.spaceImageUrl
                ?: spaceDto.organization.logoImageUrl

            return ReadSpaceItemResponse(
                id = spaceDto.id,
                name = spaceDto.name,
                location = spaceDto.location,
                spaceImageUrl = spaceImageUrl,
                openingTime = spaceDto.openingTime,
                closingTime = spaceDto.closingTime,
                capacity = spaceDto.capacity,
            )
        }
    }
}
