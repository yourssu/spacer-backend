package com.yourssu.spacer.spacehub.implement.domain.reservation

import com.yourssu.spacer.spacehub.implement.support.exception.ReservationNotFoundException
import com.yourssu.spacer.spacehub.implement.domain.space.Space
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class ReservationReader(
    private val reservationRepository: ReservationRepository,
) {

    fun isTimeConflict(reservation: Reservation): Boolean {
        return reservationRepository.existsBySpaceIdAndDateTimeRange(
            reservation.space.id!!,
            reservation.getStartDateTime(),
            reservation.getEndDateTime(),
        )
    }

    fun getAllBySpaceAndDate(space: Space, date: LocalDate): List<Reservation> {
        val startOfDay: LocalDateTime = date.atStartOfDay()
        val endOfDay: LocalDateTime = date.atTime(LocalTime.MAX)

        return reservationRepository.findAllBySpaceIdAndDateTimeRange(space.id!!, startOfDay, endOfDay)
    }

    fun getAllBySpaceAndTimeAfter(space: Space, time: LocalDateTime): List<Reservation> {
        return reservationRepository.findAllBySpaceIdAndTimeAfter(space.id!!, time)
    }

    fun getById(reservationId: Long): Reservation {
        return reservationRepository.findById(reservationId)
            ?: throw ReservationNotFoundException("예약을 찾을 수 없습니다.")
    }
}
