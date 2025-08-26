package com.yourssu.spacer.spacehub.application.domain.reservation

import com.yourssu.spacer.spacehub.business.domain.reservation.ReservationDto
import java.time.LocalDateTime

data class ReadReservationResponse(
    val id: Long,
    val name: String,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
) {

    companion object {
        fun from(dto: ReservationDto): ReadReservationResponse {
            return ReadReservationResponse(
                id = dto.id,
                name = dto.bookerName,
                startDateTime = dto.startDateTime,
                endDateTime = dto.endDateTime,
            )
        }
    }
}
