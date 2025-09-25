package com.yourssu.spacer.spacehub.implement.domain.meeting

interface RegularMeetingRepository {

    fun save(regularMeeting: RegularMeeting): RegularMeeting

    fun findActiveBySpaceId(spaceId: Long): List<RegularMeeting>

    fun findById(meetingId: Long): RegularMeeting?

    fun delete(regularMeeting: RegularMeeting)
}
