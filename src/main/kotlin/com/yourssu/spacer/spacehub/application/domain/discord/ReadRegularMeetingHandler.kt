package com.yourssu.spacer.spacehub.application.domain.discord

import com.yourssu.spacer.spacehub.application.support.constants.DiscordConstants
import com.yourssu.spacer.spacehub.business.domain.meeting.RegularMeetingService
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.awt.Color
import java.time.format.TextStyle
import java.util.*

@Component
class ReadRegularMeetingHandler(
    private val uiFactory: DiscordUIFactory,
    private val regularMeetingService: RegularMeetingService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun handleSlashCommand(event: SlashCommandInteractionEvent) {
        val organizationId = uiFactory.getVerifiedOrganizationId(event) ?: return

        val selectMenu = uiFactory.createSpaceSelectMenu(
            componentId = DiscordConstants.REGULAR_MEETING_READ_SPACE_SELECT,
            placeholder = "조회할 공간 선택",
            organizationId = organizationId
        )

        event.reply("정기 회의를 조회할 공간을 선택하세요.")
            .addActionRow(selectMenu)
            .setEphemeral(true)
            .queue()
    }

    fun handleSelectMenu(event: StringSelectInteractionEvent) {
        val spaceId = event.selectedOptions.first().value.toLong()

        try {
            val result = regularMeetingService.readActiveRegularMeetings(spaceId)
            val embed = EmbedBuilder()
                .setTitle("📅 정기 회의 목록")
                .setDescription("선택한 공간의 정기 회의입니다.")
                .setColor(Color.CYAN)

            if (result.regularMeetingDtos.isEmpty()) {
                embed.addField("결과 없음", "등록된 정기 회의가 없습니다.", false)
                event.replyEmbeds(embed.build()).setEphemeral(true).queue()
            } else {
                result.regularMeetingDtos.forEach { meeting ->
                    val title = "${meeting.startDate} ~ ${meeting.endDate} (${meeting.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN)})"
                    val content = "**팀명:** ${meeting.teamName}\n**시간:** ${meeting.startTime} ~ ${meeting.endTime}"
                    embed.addField(title, content, false)
                }

                event.replyEmbeds(embed.build())
                    .setEphemeral(true)
                    .queue()
            }
        } catch (e: Exception) {
            event.replyError("알 수 없는 오류가 발생하여 조회에 실패했습니다.")
            log.error("Unknown exception: ", e)
        }
    }
}
