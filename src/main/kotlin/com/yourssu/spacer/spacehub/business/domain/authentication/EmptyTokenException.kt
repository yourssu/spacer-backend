package com.yourssu.spacer.spacehub.business.domain.authentication

class EmptyTokenException(
    override val message: String,
) : RuntimeException(message)
