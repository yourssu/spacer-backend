package com.yourssu.spacer.spacehub.storage.support.exception

class StoreFailureException(
    override val message: String,
    override val cause: Throwable? = null,
) : RuntimeException(message)
