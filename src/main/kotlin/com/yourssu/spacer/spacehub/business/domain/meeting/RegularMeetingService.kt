package com.yourssu.spacer.spacehub.business.domain.meeting

import com.yourssu.spacer.spacehub.implement.domain.meeting.RegularMeeting
import com.yourssu.spacer.spacehub.implement.domain.meeting.RegularMeetingMapper
import com.yourssu.spacer.spacehub.implement.domain.meeting.RegularMeetingReader
import com.yourssu.spacer.spacehub.implement.domain.meeting.RegularMeetingWriter
import com.yourssu.spacer.spacehub.implement.domain.reservation.RecurringReservationCreator
import com.yourssu.spacer.spacehub.implement.domain.reservation.RecurringReservationParam
import com.yourssu.spacer.spacehub.implement.domain.reservation.ReservationTime
import com.yourssu.spacer.spacehub.implement.domain.reservation.ReservationValidator
import com.yourssu.spacer.spacehub.implement.domain.space.Space
import com.yourssu.spacer.spacehub.implement.domain.space.SpaceReader
import com.yourssu.spacer.spacehub.implement.support.security.password.EncryptPasswordEncoder
import com.yourssu.spacer.spacehub.implement.support.security.password.PasswordFormat
import com.yourssu.spacer.spacehub.implement.support.security.password.PasswordValidator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class RegularMeetingService(
    private val spaceReader: SpaceReader,
    private val regularMeetingMapper: RegularMeetingMapper,
    private val regularMeetingWriter: RegularMeetingWriter,
    private val regularMeetingReader: RegularMeetingReader,
    private val passwordEncoder: EncryptPasswordEncoder,
    private val reservationValidator: ReservationValidator,
    private val recurringReservationCreator: RecurringReservationCreator
) {

    @Transactional
    fun createRegularMeeting(command: CreateRegularMeetingCommand): List<LocalDate> {
        val space: Space = spaceReader.getById(command.spaceId)

        if (command.password != null) {
            passwordEncoder.matchesOrThrow(command.password, space.getEncryptedReservationPassword(), "예약 비밀번호가 일치하지 않습니다.")
        }
        PasswordValidator.validate(PasswordFormat.PERSONAL_RESERVATION_PASSWORD, command.rawPersonalPassword)
        val encryptedPersonalPassword: String = passwordEncoder.encode(command.rawPersonalPassword)

        val regularMeeting = regularMeetingMapper.toRegularMeeting(space, command, encryptedPersonalPassword);
        val savedRegularMeeting: RegularMeeting = regularMeetingWriter.write(regularMeeting);

        val representativeReservationTime = ReservationTime.of(command.startDate, command.startTime, command.endTime)
        reservationValidator.validateTime(space, representativeReservationTime)
        val param = RecurringReservationParam.of(space, savedRegularMeeting, command, encryptedPersonalPassword)
        return recurringReservationCreator.create(param)
    }

    fun readActiveRegularMeetings(spaceId: Long): ReadRegularMeetingsResult {
        val space: Space = spaceReader.getById(spaceId)
        val regularMeetings: List<RegularMeeting> = regularMeetingReader.findActiveBySpace(space)

        return ReadRegularMeetingsResult.from(regularMeetings)
    }

    fun delete(meetingId: Long, personalPassword: String) {
        val regularMeeting: RegularMeeting = regularMeetingReader.getById(meetingId);
        passwordEncoder.matchesOrThrow(personalPassword, regularMeeting.encryptedPersonalPassword, "예약 시 사용한 비밀번호와 일치하지 않습니다.")
        regularMeetingWriter.delete(regularMeeting)
    }
}
