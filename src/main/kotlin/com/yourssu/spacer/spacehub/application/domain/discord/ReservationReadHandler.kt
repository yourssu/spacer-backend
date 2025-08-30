package com.yourssu.spacer.spacehub.application.domain.discord

import com.yourssu.spacer.spacehub.business.domain.reservation.ReservationService
import com.yourssu.spacer.spacehub.business.domain.space.SpaceService
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import org.springframework.stereotype.Component
import java.awt.Color
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Component
class ReservationReadHandler(
    private val uiFactory: DiscordUIFactory,
    private val spaceService: SpaceService,
    private val reservationService: ReservationService
) {

    fun handleSlashCommand(event: SlashCommandInteractionEvent) {
        val organizationId = uiFactory.getVerifiedOrganizationId(event) ?: return

        val selectMenu = uiFactory.createSpaceSelectMenu(
            componentId = "space_select_read",
            placeholder = "조회할 공간 선택",
            organizationId = organizationId
        )

        event.reply("예약 현황을 조회할 공간을 선택하세요.")
            .addActionRow(selectMenu)
            .setEphemeral(true)
            .queue()
    }

    fun handleSelectMenu(event: StringSelectInteractionEvent) {
        val spaceId = event.selectedOptions.first().value
        val modal = Modal.create("read_reservation_modal:$spaceId", "날짜 입력")
            .addActionRow(
                TextInput.create("date", "조회할 날짜 (YYYY-MM-DD)", TextInputStyle.SHORT)
                    .setRequired(true)
                    .setPlaceholder(LocalDate.now().toString())
                    .setValue(LocalDate.now().toString())
                    .build()
            )
            .build()
        event.replyModal(modal).queue()
    }

    fun handleReadModal(event: ModalInteractionEvent) {
        val spaceId = event.modalId.split(":")[1].toLong()
        val dateStr = event.getValue("date")!!.asString
        val date = try {
            LocalDate.parse(dateStr)
        } catch (e: DateTimeParseException) {
            event.reply("❌ 날짜 형식이 올바르지 않습니다. 'YYYY-MM-DD' 형식으로 입력해주세요.").setEphemeral(true).queue()
            return
        }
        val result = reservationService.readAllByDate(spaceId, date)
        val space = spaceService.readById(spaceId)
        val embed = EmbedBuilder()
            .setTitle("📅 ${space.name} 예약 현황")
            .setDescription("**${date}** 의 예약 목록입니다.")
            .setColor(Color.CYAN)
        if (result.reservationDtos.isEmpty()) {
            embed.addField("결과 없음", "해당 날짜에 예약이 없습니다.", false)
        } else {
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            result.reservationDtos.sortedBy { it.startDateTime }.forEach { reservation ->
                val title = "${reservation.startDateTime.format(timeFormatter)} ~ ${reservation.endDateTime.format(timeFormatter)}"
                val booker = "예약자: ${reservation.bookerName}"
                embed.addField(title, booker, false)
            }
        }
        event.replyEmbeds(embed.build()).setEphemeral(true).queue()
    }
}