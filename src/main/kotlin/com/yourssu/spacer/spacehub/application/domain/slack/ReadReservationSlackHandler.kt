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
import com.yourssu.spacer.spacehub.application.support.utils.InputParser
import com.yourssu.spacer.spacehub.business.domain.reservation.ReservationService
import com.yourssu.spacer.spacehub.business.domain.space.SpaceService
import com.yourssu.spacer.spacehub.implement.domain.slack.SlackWorkspaceLinkReader
import org.joda.time.LocalDate
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter

@Component
class ReadReservationSlackHandler(
    private val uiFactory: SlackUIFactory,
    private val spaceService: SpaceService,
    private val reservationService: ReservationService,
    private val slackWorkspaceLinkReader: SlackWorkspaceLinkReader,
    private val slackReplyHelper: SlackReplyHelper,
    private val inputParser: InputParser
) : SlackSlashHandler, SlackBlockActionHandler, SlackViewSubmissionHandler {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override val command = Commands.RESERVATION_READ
    override val actionId = SlackConstants.RESERVATION_READ_SPACE_SELECT
    override val callbackId = SlackConstants.RESERVATION_READ_MODAL_SUBMIT

    override fun handle(req: SlashCommandRequest, ctx: SlashCommandContext): Response {
        val organizationId = uiFactory.getVerifiedOrganizationId(ctx.teamId)
            ?: return ctx.ack(SlackConstants.Messages.UNLINKED_WORKSPACE)

        val spaceSelectMenu = uiFactory.createSpaceSelectMenu(
            actionId = actionId,
            text = "예약을 조회할 공간을 선택해주세요.",
            placeholder = "조회할 공간 선택",
            organizationId = organizationId
        )

        return ctx.ack { it.responseType("ephemeral").blocks(listOf(spaceSelectMenu)) }
    }

    override fun handle(req: BlockActionPayload, ctx: ActionContext): Response {
        try {
            val selectedValue = req.actions?.firstOrNull()?.selectedOption?.value
            val channelId = req.container.channelId
            if (selectedValue == null) {
                ctx.respond { it.responseType("ephemeral").text(":warning: 공간 선택이 올바르지 않습니다.") }
                return ctx.ack()
            }

            val link = slackWorkspaceLinkReader.getByTeamId(ctx.teamId)
            val botToken = link.accessToken

            val spaceId = selectedValue
            val modalView = buildReadReservationModal(spaceId, channelId)

            val apiResponse = ctx.client().viewsOpen { it.token(botToken).triggerId(ctx.triggerId).view(modalView) }
            if (!apiResponse.isOk) {
                logger.error("예약 조회 모달 열기 API 호출 실패: {}", apiResponse.error)
                ctx.respond { it.responseType("ephemeral").text(":x: 모달을 여는 중 오류가 발생했습니다: `${apiResponse.error}`") }
            }
        } catch (e: Exception) {
            logger.error("예약 조회 모달 열기 로직에서 예외 발생", e)
            ctx.respond { it.responseType("ephemeral").text(":x: 모달을 여는 중 내부 서버 오류가 발생했습니다.") }
        }
        return ctx.ack()
    }

    override fun handle(req: ViewSubmissionPayload, ctx: ViewSubmissionContext): Response {
        val metadataParts = req.view.privateMetadata.split(":")
        val channelId = metadataParts[0]
        val spaceId = metadataParts[1].toLong()

        try {
            val dateStr = req.view.state.values[SlackConstants.BlockIds.RESERVATION_DATE]
                ?.get(SlackConstants.ActionIds.RESERVATION_DATE)?.selectedDate
                ?: throw InputParseException("날짜를 선택해주세요.")

            val date = inputParser.parseDate(dateStr)
            val reservations = reservationService.readAllByDate(spaceId, date)
            val space = spaceService.readById(spaceId)

            val message = if (reservations.reservationDtos.isEmpty()) {
                "📅 *${space.name}* (${date}) 예약 현황\n해당 날짜에 예약이 없습니다."
            } else {
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                val reservationList = reservations.reservationDtos
                    .sortedBy { it.startDateTime }
                    .joinToString("\n") {
                        val startTime = it.startDateTime.format(timeFormatter)
                        val endTime = it.endDateTime.format(timeFormatter)
                        "• `${startTime}` ~ `${endTime}` (예약자: ${it.bookerName})"
                    }
                "📅 *${space.name}* (${date}) 예약 현황\n$reservationList"
            }

            slackReplyHelper.sendSuccess(ctx, channelId, message)

        } catch (e: InputParseException) {
            slackReplyHelper.sendError(ctx, channelId, "입력 오류: ${e.message}")
        } catch (e: Exception) {
            slackReplyHelper.sendError(ctx, channelId, "알 수 없는 오류가 발생하여 조회에 실패했습니다.")
            logger.error("알 수 없는 예약 조회 오류", e)
        }

        return ctx.ack()
    }

    private fun buildReadReservationModal(spaceId: String, channelId: String): View {
        return Views.view { view ->
            view.callbackId(callbackId)
                .privateMetadata("$channelId:$spaceId")
                .type("modal")
                .title(Views.viewTitle { it.type("plain_text").text("날짜 선택") })
                .submit(Views.viewSubmit { it.type("plain_text").text("조회") })
                .close(Views.viewClose { it.type("plain_text").text("취소") })
                .blocks(
                    Blocks.asBlocks(
                        Blocks.input {
                            it.blockId(SlackConstants.BlockIds.RESERVATION_DATE)
                                .label(BlockCompositions.plainText("조회할 날짜"))
                                .element(
                                    BlockElements.datePicker { p ->
                                        p.actionId(SlackConstants.ActionIds.RESERVATION_DATE)
                                            .initialDate(LocalDate.now().toString())
                                    }
                                )
                        }
                    )
                )
        }
    }
}