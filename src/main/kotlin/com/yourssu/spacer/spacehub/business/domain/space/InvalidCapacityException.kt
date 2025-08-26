package com.yourssu.spacer.spacehub.business.domain.space

class InvalidCapacityException(
    override val message: String
) : RuntimeException(message)
