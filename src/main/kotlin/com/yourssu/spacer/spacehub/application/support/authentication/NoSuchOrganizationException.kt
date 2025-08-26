package com.yourssu.spacer.spacehub.application.support.authentication

class NoSuchOrganizationException(
    override val message: String
) : RuntimeException(message)
