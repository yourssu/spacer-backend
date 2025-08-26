package com.yourssu.spacer.spacehub.business.domain.organization

class DuplicateEmailException(
    override val message: String
) : RuntimeException(message)
