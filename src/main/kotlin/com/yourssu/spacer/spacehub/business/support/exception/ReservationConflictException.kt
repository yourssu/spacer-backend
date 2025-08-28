package com.yourssu.spacer.spacehub.business.support.exception

class ReservationConflictException(
    override val message: String
) : RuntimeException(message)
