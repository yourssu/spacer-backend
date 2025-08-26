package com.yourssu.spacer.spacehub.storage.domain.reservation

import com.yourssu.spacer.spacehub.business.domain.reservation.Reservation
import com.yourssu.spacer.spacehub.business.domain.reservation.ReservationRepository
import java.time.LocalDateTime
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class ReservationRepositoryImpl(
    private val jpaReservationRepository: JpaReservationRepository,
) : ReservationRepository {

    override fun save(reservation: Reservation): Reservation {
        return jpaReservationRepository.save(ReservationEntity.from(reservation)).toDomain()
    }

    override fun existsBySpaceIdAndDateTimeRange(
        spaceId: Long,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): Boolean {
        return jpaReservationRepository.existsBySpaceIdAndDateTimeRange(spaceId, startDateTime, endDateTime)
    }

    override fun findById(id: Long): Reservation? {
        return jpaReservationRepository.findByIdOrNull(id)?.toDomain()
    }

    override fun findAllBySpaceIdAndDateTimeRange(
        spaceId: Long,
        startOfDay: LocalDateTime,
        endOfDay: LocalDateTime
    ): List<Reservation> {
        return jpaReservationRepository.findAllBySpaceIdAndDateRange(spaceId, startOfDay, endOfDay)
            .map { it.toDomain() }
    }

    override fun findAllBySpaceIdAndTimeAfter(spaceId: Long, time: LocalDateTime): List<Reservation> {
        return jpaReservationRepository.findAllBySpaceIdAndStartDateTimeAfterOrderByStartDateTimeAsc(spaceId, time)
            .map { it.toDomain() }
    }

    override fun delete(reservation: Reservation) {
        jpaReservationRepository.deleteById(reservation.id!!)
    }
}
