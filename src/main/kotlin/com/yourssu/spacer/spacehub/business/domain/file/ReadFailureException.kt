package com.yourssu.spacer.spacehub.business.domain.file

class ReadFailureException(
    override val message: String,
    override val cause: Throwable? = null,
) : RuntimeException(message)
