package com.yourssu.spacer.spacehub.implement.domain.meeting

import com.yourssu.spacer.spacehub.implement.domain.space.Space
import com.yourssu.spacer.spacehub.implement.support.exception.RegularMeetingNotFoundException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class RegularMeetingReader(
    private val regularMeetingRepository: RegularMeetingRepository
) {

    fun getById(meetingId: Long): RegularMeeting {
        return regularMeetingRepository.findById(meetingId)
            ?: throw RegularMeetingNotFoundException("정기 회의를 찾을 수 없습니다.")
    }

    fun getAllBySpace(space: Space): List<RegularMeeting> {
        return regularMeetingRepository.findAllBySpaceId(space.id!!)
    }
}
