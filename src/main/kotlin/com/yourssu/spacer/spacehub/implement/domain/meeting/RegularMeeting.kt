package com.yourssu.spacer.spacehub.implement.domain.meeting

import com.yourssu.spacer.spacehub.implement.domain.space.Space
import com.yourssu.spacer.spacehub.implement.support.exception.InvalidMeetingDateException
import com.yourssu.spacer.spacehub.implement.support.exception.InvalidReservationTimeException
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class RegularMeeting(
    val id: Long? = null,
    val space: Space,
    val organizerName: String,
    val teamName: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime
) {
    init {
        if (startDate.isAfter(endDate)) {
            throw InvalidMeetingDateException("시작일이 종료일보다 늦습니다.")
        }

        if (startTime >= endTime) {
            throw InvalidReservationTimeException("회의 시작 시간이 종료 시간보다 늦거나 같습니다.")
        }

        if (startTime.plusHours(24) <= endTime) {
            throw InvalidReservationTimeException("하루(24시간) 이상의 회의는 등록할 수 없습니다.")
        }
    }
}
