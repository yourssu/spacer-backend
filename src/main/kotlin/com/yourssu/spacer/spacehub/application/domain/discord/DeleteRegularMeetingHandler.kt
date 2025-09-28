package com.yourssu.spacer.spacehub.application.domain.discord

import com.yourssu.spacer.spacehub.application.support.constants.DiscordConstants
import com.yourssu.spacer.spacehub.business.domain.meeting.RegularMeetingService
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
import java.time.format.TextStyle
import java.util.Locale

@Component
class DeleteRegularMeetingHandler(
    private val regularMeetingService: RegularMeetingService,
    private val uiFactory: DiscordUIFactory
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun handleSlashCommand(event: SlashCommandInteractionEvent) {
        val organizationId = uiFactory.getVerifiedOrganizationId(event) ?: return
        val selectMenu = uiFactory.createSpaceSelectMenu(
            componentId = DiscordConstants.REGULAR_MEETING_DELETE_SPACE_SELECT,
            placeholder = "삭제할 공간 선택",
            organizationId = organizationId
        )
        event.reply("정기 회의를 삭제할 공간을 선택하세요.")
            .addActionRow(selectMenu)
            .setEphemeral(true)
            .queue()
    }

    fun handleSelectMenu(event: StringSelectInteractionEvent) {
        if (event.componentId.startsWith(DiscordConstants.REGULAR_MEETING_DELETE_SPACE_SELECT)) {
            processSpaceSelect(event)
        } else {
            processMeetingSelect(event)
        }
    }

    private fun processSpaceSelect(event: StringSelectInteractionEvent) {
        val spaceId = event.selectedOptions.first().value.toLong()
        try {
            val result = regularMeetingService.readActiveRegularMeetings(spaceId)
            val embed = EmbedBuilder()
                .setTitle("🗑️ 정기 회의 삭제")
                .setDescription("삭제할 정기 회의를 선택하세요.")
                .setColor(Color.ORANGE)

            if (result.regularMeetingDtos.isEmpty()) {
                embed.addField("결과 없음", "등록된 정기 회의가 없습니다.", false)
                event.replyEmbeds(embed.build()).setEphemeral(true).queue()
            } else {
                val deleteMenu = StringSelectMenu.create(DiscordConstants.REGULAR_MEETING_DELETE_REGULAR_MEETING_SELECT)
                    .setPlaceholder("삭제할 정기 회의를 선택하세요")
                    .addOptions(
                        result.regularMeetingDtos.map {
                            val label = "${it.teamName} (${it.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN)} ${it.startTime})"
                            SelectOption.of(label, it.id.toString())
                        }
                    )
                    .build()

                event.replyEmbeds(embed.build())
                    .addActionRow(deleteMenu)
                    .setEphemeral(true)
                    .queue()
            }
        } catch (e: Exception) {
            event.replyError("알 수 없는 오류가 발생하여 조회에 실패했습니다.")
            log.error("Unknown exception: ", e)
        }
    }

    private fun processMeetingSelect(event: StringSelectInteractionEvent) {
        val meetingId = event.selectedOptions.first().value
        val modal = Modal.create("${DiscordConstants.REGULAR_MEETING_DELETE_MODAL}:$meetingId", "비밀번호 입력")
            .addActionRow(
                TextInput.create(DiscordConstants.EventIds.PERSONAL_PASSWORD, "회의 등록시 입력한 개인 비밀번호", TextInputStyle.SHORT)
                    .setRequired(true)
                    .build()
            )
            .build()
        event.replyModal(modal).queue()
    }

    fun handleDeleteModal(event: ModalInteractionEvent) {
        val meetingId = event.modalId.split(":")[1].toLong()
        val password = event.getValue(DiscordConstants.EventIds.PERSONAL_PASSWORD)!!.asString

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
