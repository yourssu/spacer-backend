package com.yourssu.spacer.spacehub.storage.domain.reservation

import java.time.LocalDateTime
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface JpaReservationRepository : JpaRepository<ReservationEntity, Long> {

    @Query("""
        SELECT COUNT(r) > 0
        FROM ReservationEntity r
        WHERE r.space.id = :spaceId 
        AND r.startDateTime < :endDateTime 
        AND r.endDateTime > :startDateTime
    """)
    fun existsBySpaceIdAndDateTimeRange(
        spaceId: Long,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): Boolean

    @Query("""
        SELECT r
        FROM ReservationEntity r
        WHERE r.space.id = :spaceId 
        AND r.startDateTime < :endOfDay 
        AND r.endDateTime > :startOfDay
    """)
    fun findAllBySpaceIdAndDateRange(
        spaceId: Long,
        startOfDay: LocalDateTime,
        endOfDay: LocalDateTime
    ): List<ReservationEntity>

    fun findAllBySpaceIdAndStartDateTimeAfterOrderByStartDateTimeAsc(
        spaceId: Long,
        time: LocalDateTime
    ): List<ReservationEntity>
}
