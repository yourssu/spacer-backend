package com.yourssu.spacer.spacehub.storage.domain.meeting

import com.yourssu.spacer.spacehub.implement.domain.meeting.RegularMeeting
import com.yourssu.spacer.spacehub.implement.domain.meeting.RegularMeetingRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class RegularMeetingRepositoryImpl(
    private val jpaRegularMeetingRepository: JpaRegularMeetingRepository
) : RegularMeetingRepository {

    override fun save(regularMeeting: RegularMeeting): RegularMeeting {
        return jpaRegularMeetingRepository.save(RegularMeetingEntity.from(regularMeeting)).toDomain()
    }

    override fun findActiveBySpaceId(
        spaceId: Long
    ): List<RegularMeeting> {
        return jpaRegularMeetingRepository.findActiveBySpaceId(spaceId)
            .map { it.toDomain() }
    }

    override fun findById(meetingId: Long): RegularMeeting? {
        return jpaRegularMeetingRepository.findByIdOrNull(meetingId)?.toDomain()
    }

    override fun delete(regularMeeting: RegularMeeting) {
        jpaRegularMeetingRepository.deleteById(regularMeeting.id!!)
    }
}
