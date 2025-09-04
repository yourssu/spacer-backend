package com.yourssu.spacer.spacehub.business.domain.meeting

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

data class CreateRegularMeetingCommand(
    val spaceId: Long,
    val teamName: String,
    val password: String,
    val rawPersonalPassword: String,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val startDate: LocalDate,
    val endDate: LocalDate
)