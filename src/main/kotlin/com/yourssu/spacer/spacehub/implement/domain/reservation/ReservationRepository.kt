package com.yourssu.spacer.spacehub.implement.domain.reservation

import java.time.LocalDateTime

interface ReservationRepository {

    fun save(reservation: Reservation): Reservation

    fun existsBySpaceIdAndDateTimeRange(
        spaceId: Long,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): Boolean

    fun findById(id: Long): Reservation?

    fun findAllBySpaceIdAndDateTimeRange(
        spaceId: Long,
        startOfDay: LocalDateTime,
        endOfDay: LocalDateTime
    ): List<Reservation>

    fun findAllBySpaceIdAndTimeAfter(spaceId: Long, time: LocalDateTime): List<Reservation>

    fun delete(reservation: Reservation)
}
