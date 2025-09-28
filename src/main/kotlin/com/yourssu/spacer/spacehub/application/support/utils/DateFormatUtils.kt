package com.yourssu.spacer.spacehub.application.support.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateFormatUtils {
    private val DEFAULT_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yy.MM.dd")

    fun today(): String {
        return LocalDate.now().format(DEFAULT_FORMATTER)
    }

    fun format(date: LocalDate, pattern: String = "yy.MM.dd"): String {
        val formatter = DateTimeFormatter.ofPattern(pattern)
        return date.format(formatter)
    }
}
