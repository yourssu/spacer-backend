package com.yourssu.spacer.spacehub.business.domain.password

class PasswordNotEncryptedException(
    override val message: String
) : RuntimeException(message)
