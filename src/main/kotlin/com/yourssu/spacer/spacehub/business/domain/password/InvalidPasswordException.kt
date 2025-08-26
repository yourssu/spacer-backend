package com.yourssu.spacer.spacehub.business.domain.password

class InvalidPasswordException(
    override val message: String
) : RuntimeException(message)
