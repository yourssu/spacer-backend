package com.yourssu.spacer.spacehub.application.domain.discord

import com.yourssu.spacer.spacehub.business.domain.discord.DiscordService
import com.yourssu.spacer.spacehub.business.domain.reservation.CreateReservationCommand
import com.yourssu.spacer.spacehub.business.domain.reservation.ReservationService
import com.yourssu.spacer.spacehub.business.domain.space.SpaceService
import com.yourssu.spacer.spacehub.business.support.exception.InvalidReservationException
import com.yourssu.spacer.spacehub.business.support.exception.PasswordNotMatchException
import com.yourssu.spacer.spacehub.business.support.exception.ReservationConflictException
import com.yourssu.spacer.spacehub.implement.support.exception.DiscordServerLinkNotFoundException
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class ReservationHandler(
    private val discordService: DiscordService,
    private val spaceService: SpaceService,
    private val reservationService: ReservationService
) {

    fun handleSlashCommand(event: SlashCommandInteractionEvent) {
        val discordServerId = event.guild?.id ?: run {
            event.reply("❌ 서버 정보가 없습니다.").setEphemeral(true).queue()
            return
        }

        val organizationId = try {
            discordService.getOrganizationIdByDiscordServerId(discordServerId)
        } catch (e: DiscordServerLinkNotFoundException) {
            event.reply("❌ 서버가 단체와 연동되지 않았습니다. `/서버등록` 후 사용해주세요.")
                .setEphemeral(true).queue()
            return
        } catch (e: Exception) {
            event.reply("❌ 알 수 없는 오류가 발생했습니다. 관리자에게 문의해주세요.")
                .setEphemeral(true).queue()
            return
        }

        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val spaces = spaceService.readAllByOrganizationId(organizationId).spaceDtos
        val selectMenu = StringSelectMenu.create("space_select")
            .setPlaceholder("예약할 공간 선택")
            .setRequiredRange(1, 1)
            .addOptions(
                spaces.map { space ->
                    val timeRange = "(${space.openingTime.format(formatter)} ~ ${space.closingTime.format(formatter)})"
                    SelectOption.of("${space.name} $timeRange", space.id.toString())
                }
            )
            .build()

        event.reply("예약할 공간을 선택하세요")
            .addActionRow(selectMenu)
            .setEphemeral(true)
            .queue()
    }

    fun handleSelectMenu(event: StringSelectInteractionEvent) {
        if (event.componentId != "space_select") return

        val spaceId = event.selectedOptions.first().value
        val modal = Modal.create("reservation_modal:$spaceId", "예약 정보 입력")
            .addActionRow(TextInput.create("user_name", "예약자명", TextInputStyle.SHORT).setRequired(true).build())
            .addActionRow(TextInput.create("date", "예약 날짜 (YYYY-MM-DD)", TextInputStyle.SHORT).setRequired(true).build())
            .addActionRow(TextInput.create("time_range", "예약 시간 (HH:mm~HH:mm)", TextInputStyle.SHORT).setRequired(true).build())
            .addActionRow(TextInput.create("password", "공간 비밀번호", TextInputStyle.SHORT).setRequired(true).build())
            .addActionRow(TextInput.create("raw_personal_password", "개인 비밀번호", TextInputStyle.SHORT).setRequired(true).build())
            .build()

        event.replyModal(modal).queue()
    }

    fun handleReservationModal(event: ModalInteractionEvent) {
        val spaceId = event.modalId.split(":")[1].toLong()
        val bookerName = event.getValue("user_name")!!.asString
        val dateStr = event.getValue("date")!!.asString
        val password = event.getValue("password")?.asString ?: ""
        val rawPersonalPassword = event.getValue("raw_personal_password")?.asString ?: ""
        val timeRange = event.getValue("time_range")!!.asString

        val dateRegex = Regex("""^\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\d|3[01])$""")
        val timeRegex = Regex("""^([01]\d|2[0-3]):(00|30)~([01]\d|2[0-3]):(00|30)$""")


        if (!dateRegex.matches(dateStr)) {
            event.reply("❌ 입력 오류: 날짜는 'YYYY-MM-DD' 형식으로 입력해주세요.")
                .setEphemeral(true)
                .queue()
            return
        }

        if (!timeRegex.matches(timeRange)) {
            event.reply("❌ 입력 오류: 시간은 'HH:mm~HH:mm' 형식으로 입력해주세요. (분은 00 또는 30만 가능합니다)")
                .setEphemeral(true)
                .queue()
            return
        }

        val (startTimeStr, endTimeStr) = timeRange.split("~")
        val startDateTime = LocalDateTime.parse("${dateStr}T${startTimeStr}")
        val endDateTime = LocalDateTime.parse("${dateStr}T${endTimeStr}")

        val command = CreateReservationCommand(
            spaceId = spaceId,
            bookerName = bookerName,
            startDateTime = startDateTime,
            endDateTime = endDateTime,
            password = password,
            rawPersonalPassword = rawPersonalPassword
        )

        try {
            reservationService.create(command)
            event.reply("✅ 예약 완료: $bookerName / ${dateStr} ${startTimeStr}~${endTimeStr}")
                .setEphemeral(true).queue()
        } catch (e: ReservationConflictException) {
            event.reply("❌ 예약 실패: 해당 시간은 이미 다른 예약이 있습니다.")
                .setEphemeral(true).queue()
        } catch (e: PasswordNotMatchException) {
            event.reply("❌ 예약 실패: 공간 비밀번호가 올바르지 않습니다.")
                .setEphemeral(true).queue()
        } catch (e: InvalidReservationException) {
            event.reply("❌ 예약 실패: 공간 사용 가능 시간이 아닙니다.")
                .setEphemeral(true).queue()
        }  catch (e: Exception) {
            event.reply("❌ 알 수 없는 오류가 발생했습니다. 관리자에게 문의하세요.")
                .setEphemeral(true).queue()
        }
    }
}