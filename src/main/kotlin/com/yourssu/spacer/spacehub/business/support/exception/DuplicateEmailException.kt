package com.yourssu.spacer.spacehub.business.support.exception

class DuplicateEmailException(
    override val message: String
) : RuntimeException(message)
