package com.yourssu.spacer.spacehub.business.domain.reservation

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class ReservationWriter(
    private val reservationRepository: ReservationRepository,
) {

    fun write(reservation: Reservation): Reservation {
        return reservationRepository.save(reservation)
    }

    fun delete(reservation: Reservation) {
        reservationRepository.delete(reservation)
    }
}
