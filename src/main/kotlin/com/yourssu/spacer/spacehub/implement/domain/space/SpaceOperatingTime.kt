package com.yourssu.spacer.spacehub.implement.domain.space

import com.yourssu.spacer.spacehub.implement.domain.reservation.ReservationTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class SpaceOperatingTime(
    val openingTime: LocalTime,
    val closingTime: LocalTime,
) {
    fun isAvailableTime(reservationTime: ReservationTime): Boolean {
        val startDate: LocalDate = reservationTime.startDateTime.toLocalDate()
        val openingDateTime = LocalDateTime.of(startDate, openingTime)
        val closingDateTime = LocalDateTime.of(startDate, closingTime)

        if (isSingleDayOperation()) {
            return reservationTime.isBetween(openingDateTime, closingDateTime)
        }

        return reservationTime.isBetween(openingDateTime, closingDateTime.plusDays(1L)) ||
                reservationTime.isBetween(openingDateTime.minusDays(1), closingDateTime)
    }

    private fun isSingleDayOperation(): Boolean {
        return openingTime.isBefore(closingTime)
    }
}
