package com.yourssu.spacer.spacehub.business.domain.organization

class InvalidEmailException(
    override val message: String
) : RuntimeException(message)
