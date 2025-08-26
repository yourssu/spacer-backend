package com.yourssu.spacer.spacehub.business.domain.authentication

class PasswordNotMatchException(
    override val message: String
) : RuntimeException(message)
