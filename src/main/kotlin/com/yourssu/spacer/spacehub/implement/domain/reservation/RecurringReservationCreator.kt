package com.yourssu.spacer.spacehub.implement.domain.reservation

import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@Component
class RecurringReservationCreator(
    private val reservationWriter: ReservationWriter,
    private val reservationMapper: ReservationMapper,
    private val reservationValidator: ReservationValidator
) {
    fun create(param: RecurringReservationParam): List<LocalDate> {
        val createdDates = mutableListOf<LocalDate>()
        val firstOccurrence = param.startDate.with(TemporalAdjusters.nextOrSame(param.dayOfWeek))
        var currentDate = firstOccurrence

        while (!currentDate.isAfter(param.endDate)) {
            val reservationTime = ReservationTime.of(currentDate, param.startTime, param.endTime)
            val reservation = reservationMapper.toReservation(param.space, param.bookerName, reservationTime, param.encryptedPersonalPassword)
            reservationValidator.validateConflict(reservation)

            reservationWriter.write(reservation)
            createdDates.add(currentDate)
            currentDate = currentDate.plusWeeks(1)
        }
        return createdDates
    }
}