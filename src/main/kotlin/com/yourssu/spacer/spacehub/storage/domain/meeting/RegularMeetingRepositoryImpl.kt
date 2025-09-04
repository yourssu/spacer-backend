package com.yourssu.spacer.spacehub.storage.domain.meeting

import com.yourssu.spacer.spacehub.implement.domain.meeting.RegularMeeting
import org.springframework.stereotype.Repository

@Repository
class RegularMeetingRepositoryImpl(
    private val jpaRegularMeetingRepository: JpaRegularMeetingRepository
) {

    fun save(domain: RegularMeeting): RegularMeeting {
        val saved = jpaRegularMeetingRepository.save(RegularMeetingEntity.from(domain))
        return saved.toDomain()
    }

    fun findById(id: Long): RegularMeeting? {
        return jpaRegularMeetingRepository.findById(id).orElse(null)?.toDomain()
    }

    fun delete(domain: RegularMeeting) {
        jpaRegularMeetingRepository.deleteById(domain.id!!)
    }

    fun findAll(): List<RegularMeeting> {
        return jpaRegularMeetingRepository.findAll().map { it.toDomain() }
    }
}
