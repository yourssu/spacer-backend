package com.yourssu.spacer.spacehub.implement.domain.reservation

import com.yourssu.spacer.spacehub.business.support.exception.InvalidReservationException
import com.yourssu.spacer.spacehub.business.support.exception.ReservationConflictException
import com.yourssu.spacer.spacehub.implement.domain.space.Space
import org.springframework.stereotype.Component

@Component
class ReservationValidator(
    private val reservationReader: ReservationReader
) {

    fun validateTime(space: Space, reservationTime: ReservationTime) {
        if (!space.canReserve(reservationTime)) {
            throw InvalidReservationException("공간 사용 가능 시간이 아닙니다.")
        }
    }

    fun validateConflict(reservation: Reservation) {
        if (reservationReader.isTimeConflict(reservation)) {
            throw ReservationConflictException("이미 예약된 시간입니다.")
        }
    }
}
