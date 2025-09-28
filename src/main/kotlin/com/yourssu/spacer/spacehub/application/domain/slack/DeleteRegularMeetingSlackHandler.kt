package com.yourssu.spacer.spacehub.application.domain.slack

import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload
import com.slack.api.app_backend.views.payload.ViewSubmissionPayload
import com.slack.api.bolt.context.builtin.ActionContext
import com.slack.api.bolt.context.builtin.SlashCommandContext
import com.slack.api.bolt.context.builtin.ViewSubmissionContext
import com.slack.api.bolt.request.builtin.SlashCommandRequest
import com.slack.api.bolt.response.Response
import com.slack.api.model.block.Blocks
import com.slack.api.model.block.composition.BlockCompositions
import com.slack.api.model.block.element.BlockElements
import com.slack.api.model.view.View
import com.slack.api.model.view.Views
import com.yourssu.spacer.spacehub.application.support.constants.Commands
import com.yourssu.spacer.spacehub.application.support.constants.SlackConstants
import com.yourssu.spacer.spacehub.application.support.exception.InputParseException
import com.yourssu.spacer.spacehub.business.domain.meeting.RegularMeetingDto
import com.yourssu.spacer.spacehub.business.domain.meeting.RegularMeetingService
import com.yourssu.spacer.spacehub.business.support.exception.PasswordNotMatchException
import com.yourssu.spacer.spacehub.implement.domain.slack.SlackWorkspaceLinkReader
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.format.TextStyle
import java.util.Locale
import kotlin.collections.map

@Component
class DeleteRegularMeetingSlackHandler(
    private val uiFactory: SlackUIFactory,
    private val regularMeetingService: RegularMeetingService,
    private val slackWorkspaceLinkReader: SlackWorkspaceLinkReader,
    private val slackReplyHelper: SlackReplyHelper
) : SlackSlashHandler, SlackBlockActionHandler, SlackViewSubmissionHandler {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override val command = Commands.REGULAR_MEETING_DELETE
    override val actionId = SlackConstants.REGULAR_MEETING_DELETE_SPACE_SELECT
    override val callbackId = SlackConstants.REGULAR_MEETING_DELETE_MODAL_SUBMIT

    override fun handle(req: SlashCommandRequest, ctx: SlashCommandContext): Response {
        val organizationId = uiFactory.getVerifiedOrganizationId(ctx.teamId)
            ?: return ctx.ack(SlackConstants.Messages.UNLINKED_WORKSPACE)

        val spaceSelectMenu = uiFactory.createSpaceSelectMenu(
            actionId = actionId,
            text = "정기 회의를 취소할 공간을 선택해주세요.",
            placeholder = "취소할 공간 선택",
            organizationId = organizationId
        )

        return ctx.ack { it.responseType("ephemeral").blocks(listOf(spaceSelectMenu)) }
    }

    override fun handle(req: BlockActionPayload, ctx: ActionContext): Response {
        try {
            val spaceId = req.actions?.firstOrNull()?.selectedOption?.value?.toLongOrNull()
            val channelId = req.container.channelId
            if (spaceId == null) {
                ctx.respond { it.responseType("ephemeral").text(":warning: 공간 선택이 올바르지 않습니다.") }
                return ctx.ack()
            }

            val meetings = regularMeetingService.readActiveRegularMeetings(spaceId)
            if (meetings.regularMeetingDtos.isEmpty()) {
                ctx.respond { it.responseType("ephemeral").text(":information_source: 해당 공간에는 취소할 정기 회의가 없습니다.") }
                return ctx.ack()
            }

            val link = slackWorkspaceLinkReader.getByTeamId(ctx.teamId)
            val modalView = buildDeleteModal(channelId, meetings.regularMeetingDtos)

            val apiResponse = ctx.client().viewsOpen { it.token(link.accessToken).triggerId(ctx.triggerId).view(modalView) }
            if (!apiResponse.isOk) {
                logger.error("정기 회의 취소 모달 열기 API 호출 실패: {}", apiResponse.error)
                ctx.respond { it.responseType("ephemeral").text(":x: 모달을 여는 중 오류가 발생했습니다: `${apiResponse.error}`") }
            }
        } catch (e: Exception) {
            logger.error("정기 회의 취소 모달 열기 로직에서 예외 발생", e)
            ctx.respond { it.responseType("ephemeral").text(":x: 모달을 여는 중 내부 서버 오류가 발생했습니다.") }
        }
        return ctx.ack()
    }

    override fun handle(req: ViewSubmissionPayload, ctx: ViewSubmissionContext): Response {
        try {
            val values = req.view.state.values

            val meetingId = values[SlackConstants.BlockIds.REGULAR_MEETING_SELECT]
                ?.get(SlackConstants.ActionIds.REGULAR_MEETING_SELECT)?.selectedOption?.value?.toLong()
                ?: throw InputParseException("취소할 정기 회의를 선택해주세요.")

            val password = values[SlackConstants.BlockIds.PERSONAL_PASSWORD]
                ?.get(SlackConstants.ActionIds.PERSONAL_PASSWORD)?.value
                ?: throw InputParseException("비밀번호를 입력해주세요.")

            if (password.isBlank()) throw InputParseException("비밀번호를 입력해주세요.")

            regularMeetingService.delete(meetingId, password)

            val successView = Views.view { view ->
                view.type("modal")
                    .title(Views.viewTitle { it.type("plain_text").text("취소 완료").emoji(true) })
                    .close(Views.viewClose { it.type("plain_text").text("확인").emoji(true) })
                    .blocks(Blocks.asBlocks(Blocks.section { it.text(BlockCompositions.markdownText("✅ 정기 회의가 성공적으로 취소되었습니다.")) }))
            }
            return ctx.ack { it.responseAction("update").view(successView) }

        } catch (e: PasswordNotMatchException) {
            val errors = mapOf(SlackConstants.BlockIds.PERSONAL_PASSWORD to e.message)
            return ctx.ack { it.responseAction("errors").errors(errors) }
        } catch (e: InputParseException) {
            val errors = mapOf(SlackConstants.BlockIds.REGULAR_MEETING_SELECT to e.message)
            return ctx.ack { it.responseAction("errors").errors(errors) }
        } catch (e: Exception) {
            logger.error("알 수 없는 정기 회의 취소 오류", e)
            val errorView = slackReplyHelper.createErrorView(SlackConstants.Messages.UNKNOWN_ERROR)
            return ctx.ack { it.responseAction("update").view(errorView) }
        }
    }

    private fun buildDeleteModal(channelId: String, meetings: List<RegularMeetingDto>): View {
        val options = meetings.map {
            val dayOfWeekKorean = it.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN)
            val label = "${it.teamName}: 매주 $dayOfWeekKorean ${it.startTime}~${it.endTime}"
            BlockCompositions.option(BlockCompositions.plainText(label), it.id.toString())
        }

        return Views.view { view ->
            view.callbackId(callbackId)
                .privateMetadata(channelId)
                .type("modal")
                .title(Views.viewTitle { it.type("plain_text").text("정기 회의 취소") })
                .submit(Views.viewSubmit { it.type("plain_text").text("취소 확정") })
                .close(Views.viewClose { it.type("plain_text").text("취소") })
                .blocks(
                    Blocks.asBlocks(
                        Blocks.input {
                            it.blockId(SlackConstants.BlockIds.REGULAR_MEETING_SELECT)
                                .label(BlockCompositions.plainText("취소할 정기 회의 선택"))
                                .element(
                                    BlockElements.staticSelect { s ->
                                        s.actionId(SlackConstants.ActionIds.REGULAR_MEETING_SELECT)
                                            .placeholder(BlockCompositions.plainText("정기 회의를 선택하세요"))
                                            .options(options)
                                    }
                                )
                        },
                        Blocks.input {
                            it.blockId(SlackConstants.BlockIds.PERSONAL_PASSWORD)
                                .label(BlockCompositions.plainText("개인 비밀번호"))
                                .element(
                                    BlockElements.plainTextInput { p ->
                                        p.actionId(SlackConstants.ActionIds.PERSONAL_PASSWORD)
                                            .placeholder(BlockCompositions.plainText("정기 회의 등록 시 사용한 비밀번호"))
                                    }
                                )
                        }
                    )
                )
        }
    }
}
