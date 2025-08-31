package com.yourssu.spacer.spacehub.implement.domain.reservation

import com.yourssu.spacer.spacehub.implement.support.exception.InvalidReservationTimeException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ReservationTime(
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
) {
    fun isBetween(rangeStart: LocalDateTime, rangeEnd: LocalDateTime): Boolean {
        return (rangeStart <= startDateTime) && (endDateTime <= rangeEnd)
    }

    init {
        if (startDateTime.isAfter(endDateTime)) {
            throw InvalidReservationTimeException("예약 시작 시간이 종료 시간보다 늦습니다.")
        }
        if (startDateTime.plusDays(1L) <= endDateTime) {
            throw InvalidReservationTimeException("하루(24시간) 이상의 예약은 불가능합니다.")
        }
    }

    companion object {
        fun of(date: LocalDate, startTime: LocalTime, endTime: LocalTime): ReservationTime {
            val startDateTime = LocalDateTime.of(date, startTime)
            val endDateTime = LocalDateTime.of(date, endTime)
            return ReservationTime(startDateTime, endDateTime)
        }
    }
}
