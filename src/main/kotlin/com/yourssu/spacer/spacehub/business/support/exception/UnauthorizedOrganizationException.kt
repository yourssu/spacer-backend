package com.yourssu.spacer.spacehub.business.support.exception

class UnauthorizedOrganizationException(
    override val message: String,
) : RuntimeException(message)
