package com.yourssu.spacer.spacehub.storage.domain.meeting

import org.springframework.data.jpa.repository.JpaRepository

interface JpaRegularMeetingRepository : JpaRepository<RegularMeetingEntity, Long> {

    fun findAllBySpaceId(spaceId: Long): List<RegularMeetingEntity>
}
