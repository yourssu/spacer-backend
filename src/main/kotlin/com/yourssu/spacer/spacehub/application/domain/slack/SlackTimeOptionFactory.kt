package com.yourssu.spacer.spacehub.application.domain.slack

import com.slack.api.model.block.composition.BlockCompositions
import com.slack.api.model.block.composition.OptionObject
import org.springframework.stereotype.Component
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Component
class SlackTimeOptionFactory {

    fun generateTimeOptions(openingTime: LocalTime, closingTime: LocalTime): List<OptionObject> {
        val fullDayOptions = (0..23).flatMap { h ->
            listOf(
                LocalTime.of(h, 0),
                LocalTime.of(h, 30)
            )
        } + LocalTime.of(23, 59)

        return fullDayOptions
            .filter { !it.isBefore(openingTime) && !it.isAfter(closingTime) }
            .map { time ->
                val timeStr = time.format(DateTimeFormatter.ofPattern("HH:mm"))
                BlockCompositions.option(BlockCompositions.plainText(timeStr), timeStr)
            }
    }
}