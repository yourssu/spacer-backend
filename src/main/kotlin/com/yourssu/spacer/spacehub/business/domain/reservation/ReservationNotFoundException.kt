package com.yourssu.spacer.spacehub.business.domain.reservation

class ReservationNotFoundException(
    override val message: String
) : RuntimeException(message)
