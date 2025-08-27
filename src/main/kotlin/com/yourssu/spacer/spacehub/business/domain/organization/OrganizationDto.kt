package com.yourssu.spacer.spacehub.business.domain.organization

import com.yourssu.spacer.spacehub.implement.domain.organization.Organization

data class OrganizationDto(

    val id: Long,
    val email: String,
    val name: String,
    val logoImageUrl: String,
    val description: String?,
    val hashtags: List<String> = emptyList(),
) {

    companion object {
        fun from(organization: Organization): OrganizationDto = OrganizationDto(
            id = organization.id!!,
            email = organization.getEmailValue(),
            name = organization.getNameValue(),
            logoImageUrl = organization.logoImageUrl,
            description = organization.description,
            hashtags = organization.getHashtagValues(),
        )
    }
}
