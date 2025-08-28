package com.yourssu.spacer.spacehub.business.domain.reservation

import com.yourssu.spacer.spacehub.business.support.exception.PasswordNotMatchException
import com.yourssu.spacer.spacehub.implement.support.security.password.PasswordFormat
import com.yourssu.spacer.spacehub.implement.support.security.password.PasswordValidator
import com.yourssu.spacer.spacehub.implement.domain.space.Space
import com.yourssu.spacer.spacehub.implement.domain.space.SpaceReader
import com.yourssu.spacer.spacehub.business.support.exception.InvalidReservationException
import com.yourssu.spacer.spacehub.business.support.exception.ReservationConflictException
import com.yourssu.spacer.spacehub.business.support.security.password.PasswordEncoder
import com.yourssu.spacer.spacehub.implement.domain.reservation.Reservation
import com.yourssu.spacer.spacehub.implement.domain.reservation.ReservationReader
import com.yourssu.spacer.spacehub.implement.domain.reservation.ReservationTime
import com.yourssu.spacer.spacehub.implement.domain.reservation.ReservationWriter
import java.time.LocalDate
import java.time.LocalDateTime
import org.springframework.stereotype.Service

@Service
class ReservationService(
    private val spaceReader: SpaceReader,
    private val passwordEncoder: PasswordEncoder,
    private val reservationReader: ReservationReader,
    private val reservationWriter: ReservationWriter,
) {

    fun create(command: CreateReservationCommand): Long {
        val space: Space = spaceReader.getById(command.spaceId)

        if (!passwordEncoder.matches(command.password, space.getEncryptedReservationPassword())) {
            throw PasswordNotMatchException("예약 비밀번호가 일치하지 않습니다.")
        }

        val reservationTime = ReservationTime(command.startDateTime, command.endDateTime)
        if (!space.canReserve(reservationTime)) {
            throw InvalidReservationException("공간 사용 가능 시간이 아닙니다.")
        }

        PasswordValidator.validate(PasswordFormat.PERSONAL_RESERVATION_PASSWORD, command.rawPersonalPassword)

        val encryptedPersonalPassword: String = passwordEncoder.encode(command.rawPersonalPassword)
        val reservation = Reservation(
            space = space,
            bookerName = command.bookerName,
            reservationTime = reservationTime,
            encryptedPersonalPassword = encryptedPersonalPassword,
        )

        if (reservationReader.isTimeConflict(reservation)) {
            throw ReservationConflictException("이미 예약된 시간입니다.")
        }

        val savedReservation: Reservation = reservationWriter.write(reservation)

        return savedReservation.id!!
    }

    fun readAllByDate(spaceId: Long, date: LocalDate): ReadReservationsResult {
        val space: Space = spaceReader.getById(spaceId)
        val reservations: List<Reservation> = reservationReader.getAllBySpaceAndDate(space, date)

        return ReadReservationsResult.from(reservations)
    }

    fun readAllAfterTime(spaceId: Long, time: LocalDateTime): ReadReservationsResult {
        val space: Space = spaceReader.getById(spaceId)
        val reservations: List<Reservation> = reservationReader.getAllBySpaceAndTimeAfter(space, time)

        return ReadReservationsResult.from(reservations)
    }

    fun delete(reservationId: Long, personalPassword: String) {
        val reservation: Reservation = reservationReader.getById(reservationId)
        if (!passwordEncoder.matches(personalPassword, reservation.encryptedPersonalPassword)) {
            throw PasswordNotMatchException("예약 시 사용한 비밀번호와 일치하지 않습니다.")
        }

        reservationWriter.delete(reservation)
    }
}
