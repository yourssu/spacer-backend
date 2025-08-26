package com.yourssu.spacer.spacehub.business.domain.reservation

class InvalidReservationException(
    override val message: String
) : RuntimeException(message)
