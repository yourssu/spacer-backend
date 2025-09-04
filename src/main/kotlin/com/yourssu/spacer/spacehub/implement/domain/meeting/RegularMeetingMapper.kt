package com.yourssu.spacer.spacehub.implement.domain.meeting

import com.yourssu.spacer.spacehub.implement.domain.space.Space
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@Component
class RegularMeetingMapper {

    fun toRegularMeeting(
        space: Space,
        teamName: String,
        dayOfWeek: DayOfWeek,
        startDate: LocalDate,
        endDate: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime
    ): RegularMeeting {
        return RegularMeeting(
            space = space,
            teamName = teamName,
            dayOfWeek = dayOfWeek,
            startDate = startDate,
            endDate = endDate,
            startTime = startTime,
            endTime = endTime
        )
    }
}