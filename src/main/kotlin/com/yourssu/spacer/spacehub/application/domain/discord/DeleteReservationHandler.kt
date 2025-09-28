package com.yourssu.spacer.spacehub.application.domain.discord

import com.yourssu.spacer.spacehub.application.support.constants.DiscordConstants
import com.yourssu.spacer.spacehub.application.support.exception.InputParseException
import com.yourssu.spacer.spacehub.application.support.utils.DateFormatUtils
import com.yourssu.spacer.spacehub.application.support.utils.InputParser
import com.yourssu.spacer.spacehub.business.domain.reservation.ReservationService
import com.yourssu.spacer.spacehub.business.domain.space.SpaceService
import com.yourssu.spacer.spacehub.business.support.exception.PasswordNotMatchException
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.awt.Color
import java.time.format.DateTimeFormatter

@Component
class DeleteReservationHandler(
    private val reservationService: ReservationService,
    private val uiFactory: DiscordUIFactory,
    private val spaceService: SpaceService,
    private val inputParser: InputParser
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun handleSlashCommand(event: SlashCommandInteractionEvent) {
        val organizationId = uiFactory.getVerifiedOrganizationId(event) ?: return
        val selectMenu = uiFactory.createSpaceSelectMenu(
            componentId = DiscordConstants.RESERVATION_DELETE_SPACE_SELECT,
            placeholder = "취소할 공간 선택",
            organizationId = organizationId
        )
        event.reply("예약을 취소할 공간을 선택하세요.")
            .addActionRow(selectMenu)
            .setEphemeral(true)
            .queue()
    }

    fun handleSelectMenu(event: StringSelectInteractionEvent) {
        if (event.componentId.startsWith(DiscordConstants.RESERVATION_DELETE_SPACE_SELECT)) {
            val spaceId = event.selectedOptions.first().value
            val modal = Modal.create("${DiscordConstants.RESERVATION_DELETE_MODAL}:date:$spaceId", "날짜 입력")
                .addActionRow(
                    TextInput.create(DiscordConstants.EventIds.DATE, "취소할 예약의 날짜 (YY.MM.DD)", TextInputStyle.SHORT)
                        .setRequired(true)
                        .setPlaceholder(DateFormatUtils.today())
                        .setValue(DateFormatUtils.today())
                        .build()
                )
                .build()
            event.replyModal(modal).queue()
        } else {
            val reservationId = event.selectedOptions.first().value
            val modal = Modal.create("${DiscordConstants.RESERVATION_DELETE_MODAL}:password:$reservationId", "비밀번호 입력")
                .addActionRow(
                    TextInput.create(DiscordConstants.EventIds.PERSONAL_PASSWORD, "예약 시 입력한 개인 비밀번호", TextInputStyle.SHORT)
                        .setRequired(true)
                        .build()
                )
                .build()
            event.replyModal(modal).queue()
        }
    }

    fun handleDeleteModal(event: ModalInteractionEvent) {
        val parts = event.modalId.split(":")
        val modalType = parts[1]

        if (modalType == "password") {
            processPasswordModal(event)
        } else if (modalType == "date") {
            processDateModal(event)
        }
    }

    private fun processDateModal(event: ModalInteractionEvent) {
        try {
            val spaceId = event.modalId.split(":")[2].toLong()
            val dateStr = event.getValue(DiscordConstants.EventIds.DATE)!!.asString
            val date = inputParser.parseDate(dateStr)
            val result = reservationService.readAllByDate(spaceId, date)
            val space = spaceService.readById(spaceId)

            val embed = EmbedBuilder()
                .setTitle("🗑️ ${space.name} 예약 취소")
                .setDescription("**${date}** 의 예약 목록입니다. 취소할 예약을 선택하세요.")
                .setColor(Color.ORANGE)

            if (result.reservationDtos.isEmpty()) {
                embed.addField("결과 없음", "해당 날짜에 예약이 없습니다.", false)
                event.replyEmbeds(embed.build()).setEphemeral(true).queue()
            } else {
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                val selectMenu = StringSelectMenu.create(DiscordConstants.RESERVATION_DELETE_RESERVATION_SELECT)
                    .setPlaceholder("취소할 예약을 선택하세요")
                    .addOptions(
                        result.reservationDtos.sortedBy { it.startDateTime }.map {
                            val label = "${it.startDateTime.format(timeFormatter)}~${it.endDateTime.format(timeFormatter)} (${it.bookerName})"
                            SelectOption.of(label, it.id.toString())
                        }
                    )
                    .build()
                event.replyEmbeds(embed.build())
                    .addActionRow(selectMenu)
                    .setEphemeral(true)
                    .queue()
            }
        } catch (e: InputParseException) {
            event.replyError("입력 오류: ${e.message}")
        } catch (e: Exception) {
            event.replyError("알 수 없는 오류가 발생하여 조회에 실패했습니다.")
            log.error("Unknown exception: ", e)
        }
    }

    private fun processPasswordModal(event: ModalInteractionEvent) {
        val reservationId = event.modalId.split(":")[2].toLong()
        val password = event.getValue(DiscordConstants.EventIds.PERSONAL_PASSWORD)!!.asString
        try {
            reservationService.delete(reservationId, password)
            event.replySuccess("예약이 정상적으로 취소되었습니다.")
        } catch (e: PasswordNotMatchException) {
            event.replyError("비밀번호가 일치하지 않습니다.")
        } catch (e: Exception) {
            event.replyError("알 수 없는 오류로 예약 취소에 실패했습니다.")
            log.error("Unknown exception: ", e)
        }
    }
}
