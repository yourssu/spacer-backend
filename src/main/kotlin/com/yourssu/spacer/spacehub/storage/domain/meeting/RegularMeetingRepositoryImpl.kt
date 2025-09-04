package com.yourssu.spacer.spacehub.storage.domain.meeting

import com.yourssu.spacer.spacehub.implement.domain.meeting.RegularMeeting
import com.yourssu.spacer.spacehub.implement.domain.meeting.RegularMeetingRepository
import org.springframework.stereotype.Repository

@Repository
class RegularMeetingRepositoryImpl(
    private val jpaRegularMeetingRepository: JpaRegularMeetingRepository
) : RegularMeetingRepository {

    override fun save(regularMeeting: RegularMeeting): RegularMeeting {
        return jpaRegularMeetingRepository.save(RegularMeetingEntity.from(regularMeeting)).toDomain()
    }
}
