package com.yourssu.spacer.spacehub.business.domain.organization

class OrganizationNotFoundException(
    override val message: String
) : RuntimeException(message)
