package com.yourssu.spacer.spacehub.application.domain.discord

import com.yourssu.spacer.spacehub.application.support.exception.InputParseException
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeParseException

@Component
class DiscordInputParser {

    companion object {
        private val DATE_REGEX = Regex("""^\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\d|3[01])$""")
        private val TIME_RANGE_REGEX = Regex("""^([01]\d|2[0-3]):(00|30)~([01]\d|2[0-3]):(00|30)$""")
    }

    fun parseDayOfWeek(dayOfWeekStr: String): DayOfWeek {
        return when (dayOfWeekStr.replace("요일", "")) {
            "월" -> DayOfWeek.MONDAY
            "화" -> DayOfWeek.TUESDAY
            "수" -> DayOfWeek.WEDNESDAY
            "목" -> DayOfWeek.THURSDAY
            "금" -> DayOfWeek.FRIDAY
            "토" -> DayOfWeek.SATURDAY
            "일" -> DayOfWeek.SUNDAY
            else -> throw InputParseException("요일을 '월', '화', '수' 형식으로 올바르게 입력해주세요.")
        }
    }

    fun parseDate(dateStr: String): LocalDate {
        if (!DATE_REGEX.matches(dateStr)) {
            throw InputParseException("날짜는 'YYYY-MM-DD' 형식으로 입력해주세요.")
        }
        try {
            return LocalDate.parse(dateStr)
        } catch (e: DateTimeParseException) {
            throw InputParseException("유효하지 않은 날짜입니다. (예: 2023-02-30)")
        }
    }

    fun parseTimeRange(timeRangeStr: String): Pair<LocalTime, LocalTime> {
        if (!TIME_RANGE_REGEX.matches(timeRangeStr)) {
            throw InputParseException("시간은 'HH:mm~HH:mm' 형식으로 입력해주세요. (분은 00 또는 30만 가능)")
        }
        val (startTimeStr, endTimeStr) = timeRangeStr.split("~")
        val startTime = LocalTime.parse(startTimeStr)
        val endTime = LocalTime.parse(endTimeStr)

        if (startTime.isAfter(endTime) || startTime == endTime) {
            throw InputParseException("시작 시간은 종료 시간보다 빨라야 합니다.")
        }
        return Pair(startTime, endTime)
    }

    fun parseDateRange(dateRangeStr: String): Pair<LocalDate, LocalDate> {
        val dateParts = dateRangeStr.split('~')
        if (dateParts.size != 2) {
            throw InputParseException("예약 기간을 'YYYY-MM-DD~YYYY-MM-DD' 형식으로 입력해주세요.")
        }
        val startDate = parseDate(dateParts[0])
        val endDate = parseDate(dateParts[1])

        if (startDate.isAfter(endDate)) {
            throw InputParseException("시작일은 종료일보다 이전이거나 같아야 합니다.")
        }
        return Pair(startDate, endDate)
    }

    fun parsePasswords(passwordsStr: String): Pair<String, String> {
        val passwordParts = passwordsStr.split('/')
        if (passwordParts.size != 2) {
            throw InputParseException("비밀번호를 '공간 비밀번호/개인 비밀번호' 형식으로 입력해주세요.")
        }
        return Pair(passwordParts[0], passwordParts[1])
    }
}
