package com.yourssu.spacer.spacehub.business.domain.reservation

import com.yourssu.spacer.spacehub.implement.domain.reservation.Reservation

data class ReadReservationsResult(
    val reservationDtos: List<ReservationDto>,
) {

    companion object {
        fun from(reservations: List<Reservation>): ReadReservationsResult = ReadReservationsResult(
            reservations.map {
                ReservationDto.from(it)
            }
        )
    }
}
