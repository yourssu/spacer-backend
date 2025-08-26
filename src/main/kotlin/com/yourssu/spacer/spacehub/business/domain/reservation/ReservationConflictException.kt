package com.yourssu.spacer.spacehub.business.domain.reservation

class ReservationConflictException(
    override val message: String
) : RuntimeException(message)
