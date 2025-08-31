package com.yourssu.spacer.spacehub.business.domain.reservation

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

data class CreateRecurringReservationCommand(
    val spaceId: Long,
    val bookerName: String,
    val password: String,
    val rawPersonalPassword: String,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val startDate: LocalDate,
    val endDate: LocalDate
)