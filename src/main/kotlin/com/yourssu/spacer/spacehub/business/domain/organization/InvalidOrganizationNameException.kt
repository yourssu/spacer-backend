package com.yourssu.spacer.spacehub.business.domain.organization

class InvalidOrganizationNameException(
    override val message: String
) : RuntimeException(message)
