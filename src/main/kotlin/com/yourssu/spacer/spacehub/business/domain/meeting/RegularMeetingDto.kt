package com.yourssu.spacer.spacehub.business.domain.meeting

import com.yourssu.spacer.spacehub.implement.domain.meeting.RegularMeeting
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

data class RegularMeetingDto(
    val id: Long,
    val teamName: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime
) {
    companion object {
        fun from(meeting: RegularMeeting): RegularMeetingDto {
            return RegularMeetingDto(
                id = meeting.id!!,
                teamName = meeting.teamName,
                startDate = meeting.startDate,
                endDate = meeting.endDate,
                dayOfWeek = meeting.dayOfWeek,
                startTime = meeting.startTime,
                endTime = meeting.endTime
            )
        }
    }
}