package com.yourssu.spacer.spacehub.business.domain.space

class SpaceNotFoundException(
    override val message: String
) : RuntimeException(message)
