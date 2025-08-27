package com.yourssu.spacer.spacehub.business.support.exception

class PasswordNotMatchException(
    override val message: String
) : RuntimeException(message)
