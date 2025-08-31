package com.yourssu.spacer.spacehub.application.domain.discord

import com.yourssu.spacer.spacehub.application.support.constants.DiscordConstants
import com.yourssu.spacer.spacehub.business.domain.reservation.CreateRecurringReservationCommand
import com.yourssu.spacer.spacehub.business.domain.reservation.ReservationService
import com.yourssu.spacer.spacehub.business.support.exception.InvalidReservationException
import com.yourssu.spacer.spacehub.business.support.exception.PasswordNotMatchException
import com.yourssu.spacer.spacehub.business.support.exception.ReservationConflictException
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.*

@Component
class CreateRecurringReservationHandler(
    private val reservationService: ReservationService,
    private val uiFactory: DiscordUIFactory
) {

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

        val modal = Modal.create("${DiscordConstants.RECURRING_RESERVATION_CREATE_MODAL}:$spaceId", "정기 회의 정보 입력")
            .addActionRow(TextInput.create("user_name", "예약자명", TextInputStyle.SHORT).setRequired(true).build())
            .addActionRow(TextInput.create("day_of_week", "요일 (월, 화, 수...)", TextInputStyle.SHORT).setRequired(true).build())
            .addActionRow(TextInput.create("time_range", "예약 시간 (HH:mm~HH:mm)", TextInputStyle.SHORT).setRequired(true).build())
            .addActionRow(
                TextInput.create("date_range", "예약 기간 (시작일~종료일)", TextInputStyle.SHORT)
                    .setPlaceholder("YYYY-MM-DD~YYYY-MM-DD")
                    .setRequired(true).build()
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

        val dayOfWeekStr = event.getValue("day_of_week")!!.asString.replace("요일", "")
        val timeRange = event.getValue("time_range")!!.asString

        val dateRangeStr = event.getValue("date_range")!!.asString
        val dateParts = dateRangeStr.split('~')
        if (dateParts.size != 2) {
            event.hook.sendMessage("❌ 입력 오류: 예약 기간을 'YYYY-MM-DD~YYYY-MM-DD' 형식으로 입력해주세요.").queue()
            return
        }
        val startDateStr = dateParts[0]
        val endDateStr = dateParts[1]

        val passwordsStr = event.getValue("passwords")!!.asString
        val passwordParts = passwordsStr.split('/')
        if (passwordParts.size != 2) {
            event.hook.sendMessage("❌ 입력 오류: 비밀번호를 '공간 비밀번호/개인 비밀번호' 형식으로 입력해주세요.").queue()
            return
        }
        val spacePassword = passwordParts[0]
        val personalPassword = passwordParts[1]

        val dayOfWeek = parseDayOfWeek(dayOfWeekStr)
        if (dayOfWeek == null) {
            event.hook.sendMessage("❌ 입력 오류: 요일을 '월', '화', '수' 형식으로 올바르게 입력해주세요.").queue()
            return
        }

        val timeRegex = Regex("""^([01]\d|2[0-3]):(00|30)~([01]\d|2[0-3]):(00|30)$""")
        if (!timeRegex.matches(timeRange)) {
            event.hook.sendMessage("❌ 입력 오류: 시간은 'HH:mm~HH:mm' 형식으로 입력해주세요. (분은 00 또는 30만 가능)").queue()
            return
        }

        val dateRegex = Regex("""^\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\d|3[01])$""")
        if (!dateRegex.matches(startDateStr) || !dateRegex.matches(endDateStr)) {
            event.hook.sendMessage("❌ 입력 오류: 날짜는 'YYYY-MM-DD' 형식으로 입력해주세요.").queue()
            return
        }

        val startDate = LocalDate.parse(startDateStr)
        val endDate = LocalDate.parse(endDateStr)
        if (startDate.isAfter(endDate)) {
            event.hook.sendMessage("❌ 입력 오류: 시작일은 종료일보다 이전이거나 같아야 합니다.").queue()
            return
        }

        val (startTimeStr, endTimeStr) = timeRange.split("~")
        val command = CreateRecurringReservationCommand(
            spaceId = event.modalId.split(":")[1].toLong(),
            bookerName = event.getValue("user_name")!!.asString,
            password = spacePassword,
            rawPersonalPassword = personalPassword,
            dayOfWeek = dayOfWeek,
            startTime = LocalTime.parse(startTimeStr),
            endTime = LocalTime.parse(endTimeStr),
            startDate = startDate,
            endDate = endDate
        )

        try {
            val createdDates = reservationService.createRecurring(command)

            val successMessage = """
                ✅ **정기 회의 예약 완료**
                총 **${createdDates.size}건**의 예약이 모두 성공적으로 생성되었습니다.
                
                **예약자**: ${command.bookerName}
                **기간**: ${command.startDate} ~ ${command.endDate}
                **요일**: ${command.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN)}
                **시간**: ${command.startTime}~${command.endTime}
            """.trimIndent()
            event.hook.sendMessage(successMessage).queue()

        } catch (e: ReservationConflictException) {
            event.hook.sendMessage("❌ **정기 예약 실패**: ${e.message} 모든 예약이 취소되었습니다.").queue()
        } catch (e: InvalidReservationException) {
            event.hook.sendMessage("❌ **정기 예약 실패**: ${e.message} 모든 예약이 취소되었습니다.").queue()
        } catch (e: PasswordNotMatchException) {
            event.hook.sendMessage("❌ **정기 예약 실패**: 공간 비밀번호가 올바르지 않아 모든 예약이 취소되었습니다.").queue()
        } catch (e: Exception) {
            event.hook.sendMessage("❌ **정기 예약 실패**: 알 수 없는 오류로 인해 모든 예약이 취소되었습니다. 관리자에게 문의하세요.").queue()
        }
    }

    private fun parseDayOfWeek(dayOfWeekStr: String): DayOfWeek? {
        return when (dayOfWeekStr) {
            "월" -> DayOfWeek.MONDAY
            "화" -> DayOfWeek.TUESDAY
            "수" -> DayOfWeek.WEDNESDAY
            "목" -> DayOfWeek.THURSDAY
            "금" -> DayOfWeek.FRIDAY
            "토" -> DayOfWeek.SATURDAY
            "일" -> DayOfWeek.SUNDAY
            else -> null
        }
    }
}