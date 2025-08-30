package com.yourssu.spacer.spacehub.application.domain.discord

import com.yourssu.spacer.spacehub.application.support.constants.DiscordConstants
import com.yourssu.spacer.spacehub.business.domain.reservation.ReservationService
import com.yourssu.spacer.spacehub.business.support.exception.PasswordNotMatchException
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import org.springframework.stereotype.Component

@Component
class DeleteReservationHandler(
    private val reservationService: ReservationService
){

    fun handleSelectMenu(event: StringSelectInteractionEvent) {
        val reservationId = event.selectedOptions.first().value

        val modal = Modal.create("${DiscordConstants.RESERVATION_DELETE_MODAL}:$reservationId", "비밀번호 입력")
            .addActionRow(
                TextInput.create("personal_password", "예약 시 입력한 개인 비밀번호를 입력하세요", TextInputStyle.SHORT)
                    .setRequired(true)
                    .build()
            )
            .build()

        event.replyModal(modal).queue()
    }

    fun handleDeleteModal(event: ModalInteractionEvent) {
        val reservationId = event.modalId.split(":")[1].toLong()
        val password = event.getValue("personal_password")!!.asString

        try {
            reservationService.delete(reservationId, password)
            event.reply("✅ 예약이 정상적으로 취소되었습니다.").setEphemeral(true).queue()
        } catch (e: PasswordNotMatchException) {
            event.reply("❌ 비밀번호가 일치하지 않습니다.").setEphemeral(true).queue()
        } catch (e: Exception) {
            event.reply("❌ 알 수 없는 오류로 예약 취소에 실패했습니다.").setEphemeral(true).queue()
        }
    }
}