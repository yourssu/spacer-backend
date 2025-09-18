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
import com.yourssu.spacer.spacehub.implement.domain.slack.SlackWorkspaceLinkReader
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class CreateReservationSlackHandler(
    private val reservationService: ReservationService,
    private val slackWorkspaceLinkReader: SlackWorkspaceLinkReader,
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
            text = "예약을 진행할 공간을 선택해주세요.",
            placeholder = "예약할 공간 선택",
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
            val modalView = buildReservationModal(spaceId, channelId)

            val apiResponse = ctx.client().viewsOpen { it.token(botToken).triggerId(ctx.triggerId).view(modalView) }
            if (!apiResponse.isOk) {
                logger.error("예약 생성 모달 열기 API 호출 실패: {}", apiResponse.error)
                ctx.respond { it.responseType("ephemeral").text(":x: 모달을 여는 중 오류가 발생했습니다: `${apiResponse.error}`") }
            }
        } catch (e: Exception) {
            logger.error("예약 생성 모달 열기 로직에서 예외 발생", e)
            ctx.respond { it.responseType("ephemeral").text(":x: 모달을 여는 중 내부 서버 오류가 발생했습니다.") }
        }
        return ctx.ack()
    }

    override fun handle(req: ViewSubmissionPayload, ctx: ViewSubmissionContext): Response {
        val metadataParts = req.view.privateMetadata.split(":")
        val channelId = metadataParts[0]
        val spaceId = metadataParts[1].toLong()

        try {
            val command = createCommandFromModal(req, spaceId)
            reservationService.create(command)

            val dateStr = values(req)["date_block"]?.get("date_input")?.selectedDate ?: ""
            val startTime = values(req)["start_time_block"]?.get("start_time_input")?.selectedOption?.value ?: ""
            val endTime = values(req)["end_time_block"]?.get("end_time_input")?.selectedOption?.value ?: ""

            val message = "예약 완료: ${command.bookerName} / $dateStr $startTime~$endTime"
            slackReplyHelper.sendSuccess(ctx, channelId, message)
            logger.info("슬랙 봇 예약 생성 성공: ${command.bookerName}, ${command.startDateTime} ~ ${command.endDateTime}")

        } catch (e: InputParseException) {
            val blockId = when {
                SlackConstants.Keywords.BOOKER_NAME in (e.message ?: "") -> SlackConstants.BlockIds.BOOKER_NAME
                SlackConstants.Keywords.RESERVATION_DATE in (e.message ?: "") -> SlackConstants.BlockIds.RESERVATION_DATE
                SlackConstants.Keywords.START_TIME in (e.message ?: "") -> SlackConstants.BlockIds.START_TIME
                SlackConstants.Keywords.END_TIME in (e.message ?: "") -> SlackConstants.BlockIds.END_TIME
                SlackConstants.Keywords.SPACE_PASSWORD in (e.message ?: "") -> SlackConstants.BlockIds.SPACE_PASSWORD
                SlackConstants.Keywords.PERSONAL_PASSWORD in (e.message ?: "") -> SlackConstants.BlockIds.PERSONAL_PASSWORD
                else -> SlackConstants.BlockIds.BOOKER_NAME
            }
            val errors = mapOf(blockId to e.message)
            return ctx.ack { it.responseAction("errors").errors(errors)}
        } catch (e: ReservationConflictException) {
            val errors = mapOf(SlackConstants.BlockIds.START_TIME to e.message)
            return ctx.ack { it.responseAction("errors").errors(errors) }
        } catch (e: PasswordNotMatchException) {
            val errors = mapOf(SlackConstants.BlockIds.SPACE_PASSWORD to e.message)
            return ctx.ack { it.responseAction("errors").errors(errors) }
        } catch (e: InvalidReservationException) {
            val errors = mapOf(SlackConstants.BlockIds.START_TIME to e.message)
            return ctx.ack { it.responseAction("errors").errors(errors) }
        } catch (e: Exception) {
            slackReplyHelper.sendError(ctx, channelId, "알 수 없는 오류가 발생했습니다. 관리자에게 문의하세요.")
            logger.error("알 수 없는 예약 생성 오류", e)
        }
        return ctx.ack()
    }

    private fun values(req: ViewSubmissionPayload) = req.view.state.values

    private fun createCommandFromModal(req: ViewSubmissionPayload, spaceId: Long): CreateReservationCommand {
        val values = values(req)

        try {
            val bookerName = values[SlackConstants.BlockIds.BOOKER_NAME]?.get(SlackConstants.ActionIds.BOOKER_NAME)?.value
            val dateStr = values[SlackConstants.BlockIds.RESERVATION_DATE]?.get(SlackConstants.ActionIds.RESERVATION_DATE)?.selectedDate
            val startTime = values[SlackConstants.BlockIds.START_TIME]?.get(SlackConstants.ActionIds.START_TIME)?.selectedOption?.value
            val endTime = values[SlackConstants.BlockIds.END_TIME]?.get(SlackConstants.ActionIds.END_TIME)?.selectedOption?.value
            val password = values[SlackConstants.BlockIds.SPACE_PASSWORD]?.get(SlackConstants.ActionIds.SPACE_PASSWORD)?.value
            val rawPersonalPassword = values[SlackConstants.BlockIds.PERSONAL_PASSWORD]?.get(SlackConstants.ActionIds.PERSONAL_PASSWORD)?.value

            if (bookerName.isNullOrBlank()) throw InputParseException("${SlackConstants.Keywords.BOOKER_NAME}을 입력해주세요.")
            if (dateStr.isNullOrBlank()) throw InputParseException("${SlackConstants.Keywords.RESERVATION_DATE}를 선택해주세요.")
            if (startTime.isNullOrBlank()) throw InputParseException("${SlackConstants.Keywords.START_TIME}을 선택해주세요.")
            if (endTime.isNullOrBlank()) throw InputParseException("${SlackConstants.Keywords.END_TIME}을 선택해주세요.")
            if (password.isNullOrBlank()) throw InputParseException("${SlackConstants.Keywords.SPACE_PASSWORD}를 입력해주세요.")
            if (rawPersonalPassword.isNullOrBlank()) throw InputParseException("${SlackConstants.Keywords.PERSONAL_PASSWORD}를 입력해주세요.")

            val timeRangeStr = "$startTime~$endTime"
            val (startLocalTime, endLocalTime) = inputParser.parseTimeRange(timeRangeStr)

            val date = LocalDate.parse(dateStr)

            return CreateReservationCommand(
                spaceId = spaceId,
                bookerName = bookerName,
                startDateTime = LocalDateTime.of(date, startLocalTime),
                endDateTime = LocalDateTime.of(date, endLocalTime),
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

    private fun buildReservationModal(spaceId: String, channelId: String): View {
        val timeOptions = (0..23).flatMap { h ->
            listOf(
                BlockCompositions.option(BlockCompositions.plainText("%02d:00".format(h)), "%02d:00".format(h)),
                BlockCompositions.option(BlockCompositions.plainText("%02d:30".format(h)), "%02d:30".format(h))
            )
        } + listOf(BlockCompositions.option(BlockCompositions.plainText("23:59"), "23:59"))

        return Views.view { view ->
            view.callbackId(callbackId)
                .privateMetadata("$channelId:$spaceId")
                .type("modal")
                .title(Views.viewTitle { it.type("plain_text").text("예약 정보 입력") })
                .submit(Views.viewSubmit { it.type("plain_text").text("제출") })
                .close(Views.viewClose { it.type("plain_text").text("취소") })
                .blocks(
                    Blocks.asBlocks(
                        Blocks.input { it.blockId(SlackConstants.BlockIds.BOOKER_NAME).label(BlockCompositions.plainText(SlackConstants.Keywords.BOOKER_NAME)).element(BlockElements.plainTextInput { p -> p.actionId(SlackConstants.ActionIds.BOOKER_NAME) }) },
                        Blocks.input { it.blockId(SlackConstants.BlockIds.RESERVATION_DATE).label(BlockCompositions.plainText(SlackConstants.Keywords.RESERVATION_DATE)).element(BlockElements.datePicker { p -> p.actionId(SlackConstants.ActionIds.RESERVATION_DATE) }) },
                        Blocks.input { it.blockId(SlackConstants.BlockIds.START_TIME).label(BlockCompositions.plainText(SlackConstants.Keywords.START_TIME)).element(BlockElements.staticSelect { s -> s.actionId(SlackConstants.ActionIds.START_TIME).options(timeOptions) }) },
                        Blocks.input { it.blockId(SlackConstants.BlockIds.END_TIME).label(BlockCompositions.plainText(SlackConstants.Keywords.END_TIME)).element(BlockElements.staticSelect { s -> s.actionId(SlackConstants.ActionIds.END_TIME).options(timeOptions) }) },
                        Blocks.input { it.blockId(SlackConstants.BlockIds.SPACE_PASSWORD).label(BlockCompositions.plainText(SlackConstants.Keywords.SPACE_PASSWORD)).element(BlockElements.plainTextInput { p -> p.actionId(SlackConstants.ActionIds.SPACE_PASSWORD) }) },
                        Blocks.input { it.blockId(SlackConstants.BlockIds.PERSONAL_PASSWORD).label(BlockCompositions.plainText(SlackConstants.Keywords.PERSONAL_PASSWORD)).element(BlockElements.plainTextInput { p -> p.actionId(SlackConstants.ActionIds.PERSONAL_PASSWORD) }) }
                    )
                )
        }
    }
}
