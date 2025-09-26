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
import com.yourssu.spacer.spacehub.business.domain.reservation.ReservationDto
import com.yourssu.spacer.spacehub.business.domain.reservation.ReservationService
import com.yourssu.spacer.spacehub.business.support.exception.PasswordNotMatchException
import com.yourssu.spacer.spacehub.implement.domain.slack.SlackWorkspaceLinkReader
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter

@Component
class DeleteReservationSlackHandler(
    private val uiFactory: SlackUIFactory,
    private val reservationService: ReservationService,
    private val slackWorkspaceLinkReader: SlackWorkspaceLinkReader,
    private val slackReplyHelper: SlackReplyHelper,
    private val inputParser: InputParser
): SlackSlashHandler, SlackBlockActionHandler, SlackViewSubmissionHandler {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override val command = Commands.RESERVATION_DELETE
    override val actionId = SlackConstants.RESERVATION_DELETE_SPACE_SELECT
    override val callbackId = SlackConstants.RESERVATION_DELETE_MODAL_SUBMIT

    override fun handle(req: SlashCommandRequest, ctx: SlashCommandContext): Response {
        val organizationId = uiFactory.getVerifiedOrganizationId(ctx.teamId)
            ?: return ctx.ack(SlackConstants.Messages.UNLINKED_WORKSPACE)

        val spaceSelectMenu = uiFactory.createSpaceSelectMenu(
            actionId = actionId,
            text = "예약을 취소할 공간을 선택해주세요.",
            placeholder = "취소할 공간 선택",
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
            val modalView = buildDateSelectModal(selectedValue, channelId)

            val apiResponse = ctx.client().viewsOpen { it.token(link.accessToken).triggerId(ctx.triggerId).view(modalView) }
            if (!apiResponse.isOk) {
                logger.error("예약 취소 모달(날짜 선택) 열기 API 호출 실패: {}", apiResponse.error)
                ctx.respond { it.responseType("ephemeral").text(":x: 모달을 여는 중 오류가 발생했습니다: `${apiResponse.error}`") }
            }
        } catch (e: Exception) {
            logger.error("예약 취소 모달(날짜 선택) 열기 로직에서 예외 발생", e)
            ctx.respond { it.responseType("ephemeral").text(":x: 모달을 여는 중 내부 서버 오류가 발생했습니다.") }
        }
        return ctx.ack()
    }

    override fun handle(req: ViewSubmissionPayload, ctx: ViewSubmissionContext): Response {
        try {
            val viewState = req.view.state.values

            if (viewState.containsKey(SlackConstants.BlockIds.RESERVATION_DATE)) {
                return handleDateSubmission(req, ctx)
            }

            else if (viewState.containsKey(SlackConstants.BlockIds.RESERVATION_SELECT)) {
                return handleFinalSubmission(req, ctx)
            }
        } catch (e: PasswordNotMatchException) {
            val errors = mapOf(SlackConstants.BlockIds.PERSONAL_PASSWORD to e.message)
            return ctx.ack { it.responseAction("errors").errors(errors) }
        } catch (e: InputParseException) {
            val errors = mapOf(SlackConstants.BlockIds.PERSONAL_PASSWORD to e.message)
            return ctx.ack { it.responseAction("errors").errors(errors) }
        } catch (e: Exception) {
            val channelId = req.view.privateMetadata.split(":")[0]
            logger.error("알 수 없는 예약 취소 오류", e)
            slackReplyHelper.sendError(ctx, channelId, "알 수 없는 오류가 발생했습니다. 관리자에게 문의하세요.")
        }
        return ctx.ack()
    }

    private fun handleDateSubmission(req: ViewSubmissionPayload, ctx: ViewSubmissionContext): Response {
        val metadataParts = req.view.privateMetadata.split(":")
        val channelId = metadataParts[0]
        val spaceId = metadataParts[1].toLong()

        val dateStr = req.view.state.values[SlackConstants.BlockIds.RESERVATION_DATE]
            ?.get(SlackConstants.ActionIds.RESERVATION_DATE)?.selectedDate
            ?: throw InputParseException("날짜를 선택해주세요.")

        val date = inputParser.parseDate(dateStr)
        val reservations = reservationService.readAllByDate(spaceId, date)

        if (reservations.reservationDtos.isEmpty()) {
            val errors = mapOf(
                SlackConstants.BlockIds.RESERVATION_DATE to "해당 날짜에 예약이 존재하지 않습니다."
            )
            return ctx.ack { it.responseAction("errors").errors(errors) }
        }

        val updatedModalView = buildFinalModal(channelId, reservations.reservationDtos)
        return ctx.ack { it.responseAction("update").view(updatedModalView) }
    }

    private fun handleFinalSubmission(req: ViewSubmissionPayload, ctx: ViewSubmissionContext): Response {
        val channelId = req.view.privateMetadata
        val values = req.view.state.values

        val reservationId = values[SlackConstants.BlockIds.RESERVATION_SELECT]
            ?.get(SlackConstants.ActionIds.RESERVATION_SELECT)?.selectedOption?.value?.toLong()
            ?: throw InputParseException("취소할 예약을 선택해주세요.")

        val password = values[SlackConstants.BlockIds.PERSONAL_PASSWORD]
            ?.get(SlackConstants.ActionIds.PERSONAL_PASSWORD)?.value
            ?: throw InputParseException("개인 비밀번호를 입력해주세요.")

        if (password.isBlank()) throw InputParseException("개인 비밀번호를 입력해주세요.")

        reservationService.delete(reservationId, password)
        slackReplyHelper.sendSuccess(ctx, channelId, "예약이 성공적으로 취소되었습니다.")
        return ctx.ack()
    }

    private fun buildDateSelectModal(spaceId: String, channelId: String): View {
        return Views.view { view ->
            view.callbackId(callbackId)
                .privateMetadata("$channelId:$spaceId")
                .type("modal")
                .title(Views.viewTitle { it.type("plain_text").text("날짜 선택") })
                .submit(Views.viewSubmit { it.type("plain_text").text("다음") })
                .close(Views.viewClose { it.type("plain_text").text("취소") })
                .blocks(
                    Blocks.asBlocks(
                        Blocks.input {
                            it.blockId(SlackConstants.BlockIds.RESERVATION_DATE)
                                .label(BlockCompositions.plainText("취소할 예약의 날짜"))
                                .element(
                                    BlockElements.datePicker { p ->
                                        p.actionId(SlackConstants.ActionIds.RESERVATION_DATE)
                                            .initialDate(java.time.LocalDate.now().toString())
                                    }
                                )
                        }
                    )
                )
        }
    }

    private fun buildFinalModal(channelId: String, reservations: List<ReservationDto>): View {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val options = reservations.sortedBy { it.startDateTime }.map {
            val label = "${it.startDateTime.format(timeFormatter)} ~ ${it.endDateTime.format(timeFormatter)} (${it.bookerName})"
            BlockCompositions.option(BlockCompositions.plainText(label), it.id.toString())
        }

        return Views.view { view ->
            view.callbackId(callbackId)
                .privateMetadata(channelId)
                .type("modal")
                .title(Views.viewTitle { it.type("plain_text").text("예약 취소") })
                .submit(Views.viewSubmit { it.type("plain_text").text("취소 확정") })
                .close(Views.viewClose { it.type("plain_text").text("닫기") })
                .blocks(
                    Blocks.asBlocks(
                        Blocks.input {
                            it.blockId(SlackConstants.BlockIds.RESERVATION_SELECT)
                                .label(BlockCompositions.plainText("취소할 예약 선택"))
                                .element(
                                    BlockElements.staticSelect { s ->
                                        s.actionId(SlackConstants.ActionIds.RESERVATION_SELECT)
                                            .placeholder(BlockCompositions.plainText("예약을 선택하세요"))
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
                                            .placeholder(BlockCompositions.plainText("예약 시 사용한 비밀번호"))
                                    }
                                )
                        }
                    )
                )
        }
    }
}