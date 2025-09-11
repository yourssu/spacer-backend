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
import com.yourssu.spacer.spacehub.business.domain.reservation.CreateReservationCommand
import com.yourssu.spacer.spacehub.business.domain.reservation.ReservationService
import com.yourssu.spacer.spacehub.business.support.exception.InvalidReservationException
import com.yourssu.spacer.spacehub.business.support.exception.PasswordNotMatchException
import com.yourssu.spacer.spacehub.business.support.exception.ReservationConflictException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class CreateReservationSlackHandler(
    private val reservationService: ReservationService,
    private val uiFactory: SlackUIFactory,
    private val inputParser: InputParser,
    private val slackReplyHelper: SlackReplyHelper
) : SlackSlashHandler, SlackViewSubmissionHandler, SlackBlockActionHandler {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override val command = Commands.RESERVATION_CREATE
    override val actionId = SlackConstants.RESERVATION_CREATE_SPACE_SELECT
    override val callbackId = SlackConstants.RESERVATION_CREATE_MODAL_SUBMIT

    override fun handle(req: SlashCommandRequest, ctx: SlashCommandContext): Response {
        val organizationId = uiFactory.getVerifiedOrganizationId(ctx) ?: return ctx.ack()

        val spaceSelectMenu = uiFactory.createSpaceSelectMenu(
            actionId = actionId,
            placeholder = "예약할 공간 선택",
            organizationId = organizationId
        )
        return ctx.ack { it.responseType("ephemeral").blocks(listOf(spaceSelectMenu)) }
    }

    override fun handle(req: BlockActionPayload, ctx: ActionContext): Response {
        val selectedValue = req.actions?.firstOrNull()?.selectedOption?.value

        if (selectedValue == null) {
            logger.warn("공간 선택 메뉴에서 값을 찾을 수 없음: actionId=${req.actions?.firstOrNull()?.actionId}")
            ctx.respond { it.responseType("ephemeral").text(":warning: 공간 선택이 올바르지 않습니다.") }
            return ctx.ack()
        }
        val spaceId = selectedValue

        try {
            val modalView = buildReservationModal(spaceId)
            ctx.client().viewsOpen { it.triggerId(ctx.triggerId).view(modalView) }
        } catch (e: Exception) {
            logger.error("예약 생성 모달 열기 실패: spaceId=$spaceId", e)
        }
        return ctx.ack()
    }

    override fun handle(req: ViewSubmissionPayload, ctx: ViewSubmissionContext): Response {
        try {
            val command = createCommandFromModal(req)
            reservationService.create(command)

            val dateStr = values(req)["date_block"]?.get("date_input")?.selectedDate ?: ""
            val timeRangeStr = values(req)["time_range_block"]?.get("time_range_input")?.value ?: ""
            slackReplyHelper.sendSuccess(ctx, "예약 완료: ${command.bookerName} / $dateStr $timeRangeStr")

            logger.info("슬랙 봇 예약 생성 성공: ${command.bookerName}, ${command.startDateTime}")

        } catch (e: InputParseException) {
            slackReplyHelper.sendError(ctx, "입력 오류: ${e.message}")
            return ctx.ack()
        } catch (e: ReservationConflictException) {
            slackReplyHelper.sendError(ctx, "예약 실패: ${e.message}")
        } catch (e: PasswordNotMatchException) {
            slackReplyHelper.sendError(ctx, "예약 실패: ${e.message}")
        } catch (e: InvalidReservationException) {
            slackReplyHelper.sendError(ctx, "예약 실패: ${e.message}")
        } catch (e: Exception) {
            slackReplyHelper.sendError(ctx, "알 수 없는 오류가 발생했습니다. 관리자에게 문의하세요.")
            logger.error("알 수 없는 예약 생성 오류", e)
        }
        return ctx.ack()
    }

    private fun values(req: ViewSubmissionPayload) = req.view.state.values

    private fun createCommandFromModal(req: ViewSubmissionPayload): CreateReservationCommand {
        val spaceId = req.view.callbackId.split(":").last().toLong()
        val values = values(req)

        try {
            val bookerName = values["booker_name_block"]?.get("booker_name_input")?.value
            val dateStr = values["date_block"]?.get("date_input")?.selectedDate
            val timeRangeStr = values["time_range_block"]?.get("time_range_input")?.value
            val password = values["password_block"]?.get("password_input")?.value
            val rawPersonalPassword = values["personal_password_block"]?.get("personal_password_input")?.value

            if (bookerName.isNullOrBlank()) throw InputParseException("예약자명을 입력해주세요.")
            if (dateStr.isNullOrBlank()) throw InputParseException("예약 날짜를 선택해주세요.")
            if (timeRangeStr.isNullOrBlank()) throw InputParseException("예약 시간을 입력해주세요.")
            if (password.isNullOrBlank()) throw InputParseException("공간 비밀번호를 입력해주세요.")
            if (rawPersonalPassword.isNullOrBlank()) throw InputParseException("개인 비밀번호를 입력해주세요.")

            val date = LocalDate.parse(dateStr)
            val (startTime, endTime) = inputParser.parseTimeRange(timeRangeStr)

            return CreateReservationCommand(
                spaceId = spaceId,
                bookerName = bookerName,
                startDateTime = LocalDateTime.of(date, startTime),
                endDateTime = LocalDateTime.of(date, endTime),
                password = password,
                rawPersonalPassword = rawPersonalPassword
            )
        } catch (e: InputParseException) {
            throw e
        } catch (e: Exception) {
            logger.warn("모달 입력값 파싱 중 예외 발생", e)
            throw InputParseException("입력값 중 하나가 잘못된 형식입니다.")
        }
    }

    private fun buildReservationModal(spaceId: String): View {
        return Views.view { view ->
            view.callbackId("${callbackId}:${spaceId}")
                .type("modal")
                .title(Views.viewTitle { it.type("plain_text").text("예약 정보 입력") })
                .submit(Views.viewSubmit { it.type("plain_text").text("제출") })
                .close(Views.viewClose { it.type("plain_text").text("취소") })
                .blocks(
                    Blocks.asBlocks(
                        Blocks.input { it.blockId("booker_name_block").label(BlockCompositions.plainText("예약자명")).element(BlockElements.plainTextInput { p -> p.actionId("booker_name_input") }) },
                        Blocks.input { it.blockId("date_block").label(BlockCompositions.plainText("예약 날짜")).element(BlockElements.datePicker { p -> p.actionId("date_input") }) },
                        Blocks.input {
                            it.blockId("time_range_block")
                                .label(BlockCompositions.plainText("예약 시간 (HH:mm~HH:mm)"))
                                .element(
                                    BlockElements.plainTextInput { p ->
                                        p.actionId("time_range_input")
                                            .placeholder(BlockCompositions.plainText("14:00~16:30"))
                                    }
                                )
                        },
                        Blocks.input { it.blockId("password_block").label(BlockCompositions.plainText("공간 비밀번호")).element(BlockElements.plainTextInput { p -> p.actionId("password_input") }) },
                        Blocks.input { it.blockId("personal_password_block").label(BlockCompositions.plainText("개인 비밀번호")).element(BlockElements.plainTextInput { p -> p.actionId("personal_password_input") }) }
                    )
                )
        }
    }
}
