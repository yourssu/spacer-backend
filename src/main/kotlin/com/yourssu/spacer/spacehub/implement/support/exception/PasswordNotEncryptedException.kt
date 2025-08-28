package com.yourssu.spacer.spacehub.implement.support.exception

class PasswordNotEncryptedException(
    override val message: String
) : RuntimeException(message)
