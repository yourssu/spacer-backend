package com.yourssu.spacer.spacehub.business.domain.reservation

import com.yourssu.spacer.spacehub.business.domain.space.SpaceDto
import java.time.LocalDateTime

data class ReservationDto(
    val id: Long,
    val space: SpaceDto,
    val bookerName: String,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
) {

    companion object {
        fun from(reservation: Reservation): ReservationDto {
            return ReservationDto(
                id = reservation.id!!,
                space = SpaceDto.from(reservation.space),
                bookerName = reservation.bookerName,
                startDateTime = reservation.getStartDateTime(),
                endDateTime = reservation.getEndDateTime(),
            )
        }
    }
}
