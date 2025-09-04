package com.yourssu.spacer.spacehub.implement.domain.meeting

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class RegularMeetingWriter(
    private val regularMeetingRepository: RegularMeetingRepository
) {

    fun write(meeting: RegularMeeting): RegularMeeting {
        return regularMeetingRepository.save(meeting)
    }

    fun delete(regularMeeting: RegularMeeting) {
        regularMeetingRepository.delete(regularMeeting)
    }
}