package com.yourssu.spacer.spacehub.business.support.security.token

class InvalidTokenException(
    override val message: String
) : RuntimeException(message)
