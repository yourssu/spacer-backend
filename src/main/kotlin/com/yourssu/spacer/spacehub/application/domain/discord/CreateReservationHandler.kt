package com.yourssu.spacer.spacehub.application.domain.discord

import com.yourssu.spacer.spacehub.application.support.constants.DiscordConstants
import com.yourssu.spacer.spacehub.application.support.exception.InputParseException
import com.yourssu.spacer.spacehub.application.support.utils.DateFormatUtils
import com.yourssu.spacer.spacehub.application.support.utils.InputParser
import com.yourssu.spacer.spacehub.business.domain.reservation.CreateReservationCommand
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
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class CreateReservationHandler(
    private val reservationService: ReservationService,
    private val uiFactory: DiscordUIFactory,
    private val inputParser: InputParser
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun handleSlashCommand(event: SlashCommandInteractionEvent) {
        val organizationId = uiFactory.getVerifiedOrganizationId(event) ?: return

        val selectMenu = uiFactory.createSpaceSelectMenu(
            componentId = DiscordConstants.RESERVATION_CREATE_SPACE_SELECT,
            placeholder = "예약할 공간 선택",
            organizationId = organizationId
        )

        event.reply("예약할 공간을 선택하세요")
            .addActionRow(selectMenu)
            .setEphemeral(true)
            .queue()
    }

    fun handleSelectMenu(event: StringSelectInteractionEvent) {
        val spaceId = event.selectedOptions.first().value
        val modal = Modal.create("${DiscordConstants.RESERVATION_CREATE_MODAL}:$spaceId", "예약 정보 입력 (형식 맞춰서)")
            .addActionRow(TextInput.create("user_name", "예약자명", TextInputStyle.SHORT).setRequired(true).build())
            .addActionRow(
                TextInput.create("date", "예약 날짜 (YY.MM.DD, 공백 없이)", TextInputStyle.SHORT)
                    .setPlaceholder(DateFormatUtils.today())
                    .setRequired(true)
                    .build()
            )
            .addActionRow(
                TextInput.create("time_range", "예약 시간 (HH:mm~HH:mm, 공백 없이, 분은 00 or 30)", TextInputStyle.SHORT)
                    .setPlaceholder("HH:mm~HH:mm (자정까지 예약 시 23:59로 입력)")
                    .setRequired(true)
                    .build()
            )
            .addActionRow(TextInput.create("password", "공간 비밀번호", TextInputStyle.SHORT).setRequired(true).build())
            .addActionRow(
                TextInput.create("raw_personal_password", "개인 비밀번호", TextInputStyle.SHORT).setRequired(true).build()
            )
            .build()

        event.replyModal(modal).queue()
    }

    fun handleCreateModal(event: ModalInteractionEvent) {
        try {
            val command = createCommandFromModal(event)
            reservationService.create(command)

            val dateStr = event.getValue("date")!!.asString
            val timeRangeStr = event.getValue("time_range")!!.asString
            event.replySuccess("예약 완료: ${command.bookerName} / $dateStr ${timeRangeStr}")

        } catch (e: InputParseException) {
            event.replyError("입력 오류: ${e.message}")
        } catch (e: ReservationConflictException) {
            event.replyError("예약 실패: 해당 시간은 이미 다른 예약이 있습니다.")
        } catch (e: PasswordNotMatchException) {
            event.replyError("예약 실패: 공간 비밀번호가 올바르지 않습니다.")
        } catch (e: InvalidReservationException) {
            event.replyError("예약 실패: 공간 사용 가능 시간이 아닙니다.")
        } catch (e: Exception) {
            event.replyError("알 수 없는 오류가 발생했습니다. 관리자에게 문의하세요.")
            log.error("Unknown exception: ", e)
        }
    }

    private fun createCommandFromModal(event: ModalInteractionEvent): CreateReservationCommand {
        val dateStr = event.getValue("date")!!.asString
        val timeRangeStr = event.getValue("time_range")!!.asString

        val date = inputParser.parseDate(dateStr)
        val (startTime, endTime) = inputParser.parseTimeRange(timeRangeStr)

        return CreateReservationCommand(
            spaceId = event.modalId.split(":")[1].toLong(),
            bookerName = event.getValue("user_name")!!.asString,
            startDateTime = LocalDateTime.of(date, startTime),
            endDateTime = LocalDateTime.of(date, endTime),
            password = event.getValue("password")!!.asString,
            rawPersonalPassword = event.getValue("raw_personal_password")!!.asString
        )
    }
}
