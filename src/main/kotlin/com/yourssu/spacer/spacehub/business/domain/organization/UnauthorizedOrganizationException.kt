package com.yourssu.spacer.spacehub.business.domain.organization

class UnauthorizedOrganizationException(
    override val message: String,
) : RuntimeException(message)
