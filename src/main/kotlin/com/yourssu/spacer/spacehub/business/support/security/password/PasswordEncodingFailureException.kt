package com.yourssu.spacer.spacehub.business.support.security.password

class PasswordEncodingFailureException(
    override val message: String
) : RuntimeException(message)
