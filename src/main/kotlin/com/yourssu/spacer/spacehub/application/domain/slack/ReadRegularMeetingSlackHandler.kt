package com.yourssu.spacer.spacehub.application.domain.slack

import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload
import com.slack.api.bolt.context.builtin.ActionContext
import com.slack.api.bolt.context.builtin.SlashCommandContext
import com.slack.api.bolt.request.builtin.SlashCommandRequest
import com.slack.api.bolt.response.Response
import com.yourssu.spacer.spacehub.application.support.constants.Commands
import com.yourssu.spacer.spacehub.application.support.constants.SlackConstants
import com.yourssu.spacer.spacehub.business.domain.meeting.ReadRegularMeetingsResult
import com.yourssu.spacer.spacehub.business.domain.meeting.RegularMeetingService
import com.yourssu.spacer.spacehub.implement.domain.space.SpaceReader
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.format.TextStyle
import java.util.Locale
import kotlin.collections.joinToString

@Component
class ReadRegularMeetingSlackHandler(
    private val uiFactory: SlackUIFactory,
    private val regularMeetingService: RegularMeetingService,
    private val spaceReader: SpaceReader
) : SlackSlashHandler, SlackBlockActionHandler {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override val command = Commands.REGULAR_MEETING_READ
    override val actionId = SlackConstants.REGULAR_MEETING_READ_SPACE_SELECT

    override fun handle(req: SlashCommandRequest, ctx: SlashCommandContext): Response {
        val organizationId = uiFactory.getVerifiedOrganizationId(ctx.teamId)
            ?: return ctx.ack(":warning: 서버가 단체와 연동되지 않았습니다. `/워크스페이스등록` 후 사용해주세요.")

        val spaceSelectMenu = uiFactory.createSpaceSelectMenu(
            actionId = actionId,
            text = "정기 회의를 조회할 공간을 선택해주세요.",
            placeholder = "조회할 공간 선택",
            organizationId = organizationId
        )

        return ctx.ack { it.responseType("ephemeral").blocks(listOf(spaceSelectMenu)) }
    }

    override fun handle(req: BlockActionPayload, ctx: ActionContext): Response {
        try {
            val spaceId = req.actions?.firstOrNull()?.selectedOption?.value?.toLongOrNull()
            if (spaceId == null) {
                ctx.respond { it.responseType("ephemeral").text(":warning: 공간 선택이 올바르지 않습니다.") }
                return ctx.ack()
            }

            val activeMeetings = regularMeetingService.readActiveRegularMeetings(spaceId)
            val space = spaceReader.getById(spaceId)
            val message = buildMeetingListMessage(space.name, activeMeetings)
            ctx.respond { it.responseType("ephemeral").text(message) }
        } catch (e: Exception) {
            logger.error("정기 회의 조회 처리 중 예외 발생", e)
            ctx.respond { it.responseType("ephemeral").text(":x: 정기 회의를 조회하는 중 오류가 발생했습니다.") }
        }

        return ctx.ack()
    }

    private fun buildMeetingListMessage(spaceName: String, meetings: ReadRegularMeetingsResult): String {
        if (meetings.regularMeetingDtos.isEmpty()) {
            return "📅 *$spaceName* 에 등록된 활성 정기 회의가 없습니다."
        }

        val meetingList = meetings.regularMeetingDtos.joinToString("\n") { dto ->
            val dayOfWeekKorean = dto.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN)
            "• *${dto.teamName}*: 매주 $dayOfWeekKorean `${dto.startTime}`~`${dto.endTime}` (기간: ${dto.startDate} ~ ${dto.endDate})"
        }

        return """
            |📅 *$spaceName* 정기 회의 현황
            |$meetingList
        """.trimMargin()
    }
}
