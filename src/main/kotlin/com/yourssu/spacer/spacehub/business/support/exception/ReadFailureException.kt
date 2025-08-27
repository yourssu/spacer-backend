package com.yourssu.spacer.spacehub.business.support.exception

class ReadFailureException(
    override val message: String,
    override val cause: Throwable? = null,
) : RuntimeException(message)
