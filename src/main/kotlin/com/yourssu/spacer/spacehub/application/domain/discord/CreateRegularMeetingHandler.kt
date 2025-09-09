package com.yourssu.spacer.spacehub.application.domain.discord

import com.yourssu.spacer.spacehub.application.support.constants.DiscordConstants
import com.yourssu.spacer.spacehub.application.support.exception.InputParseException
import com.yourssu.spacer.spacehub.application.support.utils.InputParser
import com.yourssu.spacer.spacehub.business.domain.meeting.CreateRegularMeetingCommand
import com.yourssu.spacer.spacehub.business.domain.meeting.RegularMeetingService
import com.yourssu.spacer.spacehub.business.support.exception.InvalidReservationException
import com.yourssu.spacer.spacehub.business.support.exception.PasswordNotMatchException
import com.yourssu.spacer.spacehub.business.support.exception.ReservationConflictException
import com.yourssu.spacer.spacehub.implement.support.exception.InvalidPasswordException
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.format.TextStyle
import java.util.*

@Component
class CreateRegularMeetingHandler(
    private val regularMeetingService: RegularMeetingService,
    private val uiFactory: DiscordUIFactory,
    private val inputParser: InputParser
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun handleSlashCommand(event: SlashCommandInteractionEvent) {
        val organizationId = uiFactory.getVerifiedOrganizationId(event) ?: return

        val selectMenu = uiFactory.createSpaceSelectMenu(
            componentId = DiscordConstants.RECURRING_RESERVATION_CREATE_SPACE_SELECT,
            placeholder = "정기 회의를 진행할 공간 선택",
            organizationId = organizationId
        )

        event.reply("정기 회의를 진행할 공간을 선택하세요")
            .addActionRow(selectMenu)
            .setEphemeral(true)
            .queue()
    }

    fun handleSelectMenu(event: StringSelectInteractionEvent) {
        val spaceId = event.selectedOptions.first().value

        val modal = Modal.create("${DiscordConstants.RECURRING_RESERVATION_CREATE_MODAL}:$spaceId", "정기 회의 정보 입력 (형식 맞춰서)")
            .addActionRow(TextInput.create("team_name", "팀 이름", TextInputStyle.SHORT).setRequired(true).build())
            .addActionRow(
                TextInput.create("day_of_week", "요일 (월, 화, 수...)", TextInputStyle.SHORT)
                    .setPlaceholder("월")
                    .setRequired(true)
                    .build()
            )
            .addActionRow(
                TextInput.create("time_range", "예약 시간 (HH:mm~HH:mm, 공백 없이, 분은 00 or 30)", TextInputStyle.SHORT)
                    .setPlaceholder("HH:mm~HH:mm")
                    .setRequired(true)
                    .build()
            )
            .addActionRow(
                TextInput.create("date_range", "예약 기간 (YYYY-MM-DD~YYYY-MM-DD, 공백 없이)", TextInputStyle.SHORT)
                    .setPlaceholder("YYYY-MM-DD~YYYY-MM-DD")
                    .setRequired(true)
                    .build()
            )
            .addActionRow(
                TextInput.create("passwords", "비밀번호 (공간/개인)", TextInputStyle.SHORT)
                    .setPlaceholder("공간비밀번호/개인비밀번호 형식으로 입력")
                    .setRequired(true)
                    .build()
            )
            .build()

        event.replyModal(modal).queue()
    }

    fun handleCreateModal(event: ModalInteractionEvent) {
        event.deferReply(true).queue()

        try {
            val command = createCommandFromModal(event)
            val createdDates = regularMeetingService.createRegularMeeting(command)

            val successMessage = """
                **정기 회의 예약 완료**
                총 **${createdDates.size}건**의 예약이 모두 성공적으로 생성되었습니다.
                
                **팀명**: ${command.teamName}
                **기간**: ${command.startDate} ~ ${command.endDate}
                **요일**: ${command.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN)}
                **시간**: ${command.startTime}~${command.endTime}
            """.trimIndent()
            event.hook.sendMessage(successMessage).setEphemeral(true).queue()

        } catch (e: InputParseException) {
            event.hook.sendError("입력 오류: ${e.message}")
        } catch (e: ReservationConflictException) {
            event.hook.sendError("**정기 예약 실패**: ${e.message}")
        } catch (e: InvalidReservationException) {
            event.hook.sendError("**정기 예약 실패**: ${e.message}")
        } catch (e: PasswordNotMatchException) {
            event.hook.sendError("**정기 예약 실패**: ${e.message}")
        } catch(e: InvalidPasswordException) {
            event.hook.sendError("**정기 예약 실패**: ${e.message}")
        } catch (e: Exception) {
            event.hook.sendError("**정기 예약 실패**: 알 수 없는 오류로 정기 예약에 실패했습니다. 관리자에게 문의하세요.")
            log.error("Unknown exception: ", e)
        }
    }

    private fun createCommandFromModal(event: ModalInteractionEvent): CreateRegularMeetingCommand {
        val dayOfWeekStr = event.getValue("day_of_week")!!.asString
        val timeRangeStr = event.getValue("time_range")!!.asString
        val dateRangeStr = event.getValue("date_range")!!.asString
        val passwordsStr = event.getValue("passwords")!!.asString

        val dayOfWeek = inputParser.parseDayOfWeek(dayOfWeekStr)
        var (startTime, endTime) = inputParser.parseTimeRange(timeRangeStr)

        if (endTime == java.time.LocalTime.MIDNIGHT) {
            endTime = java.time.LocalTime.of(23, 59)
        }

        val (startDate, endDate) = inputParser.parseDateRange(dateRangeStr)
        val (spacePassword, personalPassword) = inputParser.parsePasswords(passwordsStr)

        return CreateRegularMeetingCommand(
            spaceId = event.modalId.split(":")[1].toLong(),
            teamName = event.getValue("team_name")!!.asString,
            password = spacePassword,
            rawPersonalPassword = personalPassword,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            endTime = endTime,
            startDate = startDate,
            endDate = endDate
        )
    }
}
