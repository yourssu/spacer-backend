package com.yourssu.spacer.spacehub.implement.domain.meeting

interface RegularMeetingRepository {

    fun save(regularMeeting: RegularMeeting): RegularMeeting
}