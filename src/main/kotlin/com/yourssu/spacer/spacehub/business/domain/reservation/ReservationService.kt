package com.yourssu.spacer.spacehub.business.domain.reservation

import com.yourssu.spacer.spacehub.implement.support.security.password.PasswordFormat
import com.yourssu.spacer.spacehub.implement.support.security.password.PasswordValidator
import com.yourssu.spacer.spacehub.implement.domain.space.Space
import com.yourssu.spacer.spacehub.implement.domain.space.SpaceReader
import com.yourssu.spacer.spacehub.business.support.security.password.PasswordEncoder
import com.yourssu.spacer.spacehub.implement.domain.reservation.Reservation
import com.yourssu.spacer.spacehub.implement.domain.reservation.ReservationMapper
import com.yourssu.spacer.spacehub.implement.domain.reservation.ReservationReader
import com.yourssu.spacer.spacehub.implement.domain.reservation.ReservationTime
import com.yourssu.spacer.spacehub.implement.domain.reservation.ReservationValidator
import com.yourssu.spacer.spacehub.implement.domain.reservation.ReservationWriter
import java.time.LocalDate
import java.time.LocalDateTime
import org.springframework.stereotype.Service

@Service
class ReservationService(
    private val spaceReader: SpaceReader,
    private val passwordEncoder: PasswordEncoder,
    private val reservationValidator: ReservationValidator,
    private val reservationMapper: ReservationMapper,
    private val reservationWriter: ReservationWriter,
    private val reservationReader: ReservationReader
) {

    fun create(command: CreateReservationCommand): Long {
        val space: Space = spaceReader.getById(command.spaceId)
        val reservationTime = ReservationTime(command.startDateTime, command.endDateTime)
        passwordEncoder.matchesOrThrow(command.password, space.getEncryptedReservationPassword(), "예약 비밀번호가 일치하지 않습니다.")
        reservationValidator.validateTime(space, reservationTime)

        PasswordValidator.validate(PasswordFormat.PERSONAL_RESERVATION_PASSWORD, command.rawPersonalPassword)
        val encryptedPersonalPassword: String = passwordEncoder.encode(command.rawPersonalPassword)
        val reservation = reservationMapper.toReservation(space, command.bookerName, reservationTime, encryptedPersonalPassword)
        reservationValidator.validateConflict(reservation)

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
        passwordEncoder.matchesOrThrow(personalPassword, reservation.encryptedPersonalPassword, "예약 시 사용한 비밀번호와 일치하지 않습니다.")
        reservationWriter.delete(reservation)
    }
}
