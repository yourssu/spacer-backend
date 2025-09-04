package com.yourssu.spacer.spacehub.implement.domain.reservation

import com.yourssu.spacer.spacehub.implement.domain.meeting.RegularMeeting
import com.yourssu.spacer.spacehub.implement.domain.space.Space
import org.springframework.stereotype.Component

@Component
class ReservationMapper(
) {

    fun toReservation(space: Space, bookerName: String, reservationTime: ReservationTime, encryptedPersonalPassword: String): Reservation {
        return Reservation(
            space = space,
            bookerName = bookerName,
            reservationTime = reservationTime,
            encryptedPersonalPassword = encryptedPersonalPassword,
        )
    }

    fun toReservation(space: Space, regularMeeting: RegularMeeting, bookerName: String, reservationTime: ReservationTime, encryptedPersonalPassword: String): Reservation {
        return Reservation(
            space = space,
            regularMeeting = regularMeeting,
            bookerName = bookerName,
            reservationTime = reservationTime,
            encryptedPersonalPassword = encryptedPersonalPassword,
        )
    }
}