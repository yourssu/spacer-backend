package com.yourssu.spacer.spacehub.business.domain.organization

import com.yourssu.spacer.spacehub.business.domain.authentication.TokenDto

data class CreateOrganizationResult(
    val id: Long,
    val name: String,
    val tokens: TokenDto,
) {
}
