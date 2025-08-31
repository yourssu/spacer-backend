package com.yourssu.spacer.spacehub.implement.domain.reservation

import com.yourssu.spacer.spacehub.business.domain.reservation.CreateRecurringReservationCommand
import com.yourssu.spacer.spacehub.implement.domain.space.Space
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

data class RecurringReservationParam (
    val space: Space,
    val bookerName: String,
    val encryptedPersonalPassword: String,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val startDate: LocalDate,
    val endDate: LocalDate
) {
    companion object {
        fun of(
            space: Space,
            command: CreateRecurringReservationCommand,
            encryptedPersonalPassword: String
        ): RecurringReservationParam {
            return RecurringReservationParam(
                space = space,
                bookerName = command.bookerName,
                encryptedPersonalPassword = encryptedPersonalPassword,
                dayOfWeek = command.dayOfWeek,
                startTime = command.startTime,
                endTime = command.endTime,
                startDate = command.startDate,
                endDate = command.endDate
            )
        }
    }
}