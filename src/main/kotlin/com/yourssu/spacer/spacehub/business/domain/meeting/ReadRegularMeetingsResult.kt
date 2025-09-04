package com.yourssu.spacer.spacehub.business.domain.meeting

import com.yourssu.spacer.spacehub.implement.domain.meeting.RegularMeeting

data class ReadRegularMeetingsResult(
    val regularMeetingDtos: List<RegularMeetingDto>,
) {
    companion object {
        fun from(regularMeetings: List<RegularMeeting>): ReadRegularMeetingsResult = ReadRegularMeetingsResult(
            regularMeetings.map { RegularMeetingDto.from(it) }
        )
    }
}