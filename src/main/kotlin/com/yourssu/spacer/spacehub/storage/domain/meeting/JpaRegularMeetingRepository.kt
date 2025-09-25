package com.yourssu.spacer.spacehub.storage.domain.meeting

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface JpaRegularMeetingRepository : JpaRepository<RegularMeetingEntity, Long> {

    @Query("SELECT rm FROM RegularMeetingEntity rm WHERE rm.space.id = :spaceId AND rm.endDate >= CURRENT_DATE")
    fun findActiveBySpaceId(spaceId: Long): List<RegularMeetingEntity>
}
