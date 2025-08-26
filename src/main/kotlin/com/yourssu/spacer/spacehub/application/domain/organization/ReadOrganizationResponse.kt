package com.yourssu.spacer.spacehub.application.domain.organization

import com.yourssu.spacer.spacehub.business.domain.organization.OrganizationDto

data class ReadOrganizationResponse(

    val id: Long,
    val name: String,
    val logoImageUrl: String,
    val description: String?,
    val hashtags: List<String> = emptyList(),
) {

    companion object {
        fun from(organizationDto: OrganizationDto) = ReadOrganizationResponse(
            id = organizationDto.id,
            name = organizationDto.name,
            logoImageUrl = organizationDto.logoImageUrl,
            description = organizationDto.description,
            hashtags = organizationDto.hashtags,
        )
    }
}
