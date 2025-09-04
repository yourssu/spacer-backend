package com.yourssu.spacer.spacehub.implement.domain.meeting

import com.yourssu.spacer.spacehub.business.domain.meeting.CreateRegularMeetingCommand
import com.yourssu.spacer.spacehub.implement.domain.space.Space
import org.springframework.stereotype.Component

@Component
class RegularMeetingMapper {

    fun toRegularMeeting(
        space: Space,
        command: CreateRegularMeetingCommand,
        encryptedPersonalPassword: String
    ): RegularMeeting {
        return RegularMeeting(
            space = space,
            teamName = command.teamName,
            dayOfWeek = command.dayOfWeek,
            startDate = command.startDate,
            endDate = command.endDate,
            startTime = command.startTime,
            endTime = command.endTime,
            encryptedPersonalPassword = encryptedPersonalPassword
        )
    }
}