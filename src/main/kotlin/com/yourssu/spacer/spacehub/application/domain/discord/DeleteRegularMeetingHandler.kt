package com.yourssu.spacer.spacehub.application.domain.discord

import com.yourssu.spacer.spacehub.application.support.constants.DiscordConstants
import com.yourssu.spacer.spacehub.business.domain.meeting.RegularMeetingService
import com.yourssu.spacer.spacehub.business.support.exception.PasswordNotMatchException
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class DeleteRegularMeetingHandler(
    private val regularMeetingService: RegularMeetingService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun handleSelectMenu(event: StringSelectInteractionEvent) {
        val meetingId = event.selectedOptions.first().value

        val modal = Modal.create("${DiscordConstants.REGULAR_MEETING_DELETE_MODAL}:$meetingId", "비밀번호 입력")
            .addActionRow(
                TextInput.create("personal_password", "회의 등록시 입력한 개인 비밀번호를 입력하세요", TextInputStyle.SHORT)
                    .setRequired(true)
                    .build()
            )
            .build()

        event.replyModal(modal).queue()
    }

    fun handleDeleteModal(event: ModalInteractionEvent) {
        val meetingId = event.modalId.split(":")[1].toLong()
        val password = event.getValue("personal_password")!!.asString

        try {
            regularMeetingService.delete(meetingId, password)
            event.replySuccess("정기 회의가 성공적으로 삭제되었습니다.")
        } catch (e: PasswordNotMatchException) {
            event.replyError("비밀번호가 일치하지 않습니다.")
        } catch (e: Exception) {
            event.replyError("알 수 없는 오류로 삭제에 실패했습니다.")
            log.error("Unknown exception: ", e)
        }
    }
}