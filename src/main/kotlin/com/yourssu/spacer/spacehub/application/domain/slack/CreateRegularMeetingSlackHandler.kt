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
import com.slack.api.model.block.composition.OptionObject
import com.slack.api.model.block.element.BlockElements
import com.slack.api.model.view.View
import com.slack.api.model.view.Views
import com.yourssu.spacer.spacehub.application.support.constants.SlashCommands
import com.yourssu.spacer.spacehub.application.support.constants.SlackConstants
import com.yourssu.spacer.spacehub.application.support.exception.InputParseException
import com.yourssu.spacer.spacehub.application.support.utils.InputParser
import com.yourssu.spacer.spacehub.business.domain.meeting.CreateRegularMeetingCommand
import com.yourssu.spacer.spacehub.business.domain.meeting.RegularMeetingService
import com.yourssu.spacer.spacehub.business.support.exception.InvalidReservationException
import com.yourssu.spacer.spacehub.business.support.exception.PasswordNotMatchException
import com.yourssu.spacer.spacehub.business.support.exception.ReservationConflictException
import com.yourssu.spacer.spacehub.implement.domain.slack.SlackWorkspaceLinkReader
import com.yourssu.spacer.spacehub.implement.domain.space.SpaceReader
import com.yourssu.spacer.spacehub.implement.support.exception.InvalidPasswordException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*

@Component
class CreateRegularMeetingSlackHandler(
    private val regularMeetingService: RegularMeetingService,
    private val uiFactory: SlackUIFactory,
    private val slackWorkspaceLinkReader: SlackWorkspaceLinkReader,
    private val spaceReader: SpaceReader,
    private val slackReplyHelper: SlackReplyHelper,
    private val slackTimeOptionFactory: SlackTimeOptionFactory,
    private val inputParser: InputParser,
) : SlackSlashHandler, SlackBlockActionHandler, SlackViewSubmissionHandler {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override val command = "/${SlashCommands.REGULAR_MEETING_CREATE}"
    override val actionId = SlackConstants.REGULAR_MEETING_CREATE_SPACE_SELECT
    override val callbackId = SlackConstants.REGULAR_MEETING_CREATE_MODAL_SUBMIT

    override fun handle(req: SlashCommandRequest, ctx: SlashCommandContext): Response {
        val organizationId = uiFactory.getVerifiedOrganizationId(ctx.teamId)
            ?: return ctx.ack(SlackConstants.Messages.UNLINKED_WORKSPACE)

        val selectMenu = uiFactory.createSpaceSelectMenu(
            actionId = actionId,
            text = "정기 회의를 진행할 공간을 선택해주세요.",
            placeholder = "정기 회의 공간 선택",
            organizationId = organizationId
        )
        return ctx.ack { it.responseType("ephemeral").blocks(listOf(selectMenu)) }
    }

    override fun handle(req: BlockActionPayload, ctx: ActionContext): Response {
        try {
            val spaceId = req.actions?.firstOrNull()?.selectedOption?.value?.toLongOrNull()
            val channelId = req.container.channelId
            if (spaceId == null) {
                ctx.respond { it.responseType("ephemeral").text(":warning: 공간 선택이 올바르지 않습니다.") }
                return ctx.ack()
            }
            val space = spaceReader.getById(spaceId)
            val timeOptions = slackTimeOptionFactory.generateTimeOptions(space.getOpeningTime(), space.getClosingTime())

            val link = slackWorkspaceLinkReader.getByTeamId(ctx.teamId)
            val modalView = buildModal(spaceId.toString(), channelId, timeOptions)
            val apiResponse = ctx.client().viewsOpen { it.token(link.accessToken).triggerId(ctx.triggerId).view(modalView) }
            if (!apiResponse.isOk) {
                logger.error("정기 회의 생성 모달 열기 API 호출 실패: {}", apiResponse.error)
                ctx.respond { it.responseType("ephemeral").text(":x: 모달을 여는 중 오류가 발생했습니다: `${apiResponse.error}`") }
            }
        } catch (e: Exception) {
            logger.error("정기 회의 생성 모달 열기 로직에서 예외 발생", e)
            ctx.respond { it.responseType("ephemeral").text(":x: 모달을 여는 중 내부 서버 오류가 발생했습니다.") }
        }
        return ctx.ack()
    }

    override fun handle(req: ViewSubmissionPayload, ctx: ViewSubmissionContext): Response {
        val spaceId = req.view.privateMetadata.split(":")[1].toLong()

        try {
            val command = createCommandFromModal(req, spaceId)
            val createdDates = regularMeetingService.createRegularMeeting(command)

            val successMessage = """
                *정기 회의 예약 완료*
                >✅ 총 *${createdDates.size}건*의 예약이 모두 성공적으로 생성되었습니다.
                >
                > • *팀명*: ${command.teamName}
                > • *기간*: ${command.startDate} ~ ${command.endDate}
                > • *요일*: ${command.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN)}
                > • *시간*: ${command.startTime}~${command.endTime}
            """.trimIndent()

            val successView = Views.view { view ->
                view.type("modal")
                    .title(Views.viewTitle { it.type("plain_text").text("예약 완료").emoji(true) })
                    .close(Views.viewClose { it.type("plain_text").text("확인").emoji(true) })
                    .blocks(Blocks.asBlocks(Blocks.section { it.text(BlockCompositions.markdownText(successMessage)) }))
            }
            return ctx.ack { it.responseAction("update").view(successView) }

        } catch (e: InputParseException) {
            val errorMessage = e.message ?: "입력값이 올바르지 않습니다."
            val blockId = when {
                SlackConstants.Keywords.TEAM_NAME in errorMessage -> SlackConstants.BlockIds.TEAM_NAME
                SlackConstants.Keywords.DAY_OF_WEEK in errorMessage -> SlackConstants.BlockIds.DAY_OF_WEEK
                SlackConstants.Keywords.START_TIME in errorMessage -> SlackConstants.BlockIds.START_TIME
                SlackConstants.Keywords.END_TIME in errorMessage -> SlackConstants.BlockIds.END_TIME
                SlackConstants.Keywords.START_DATE in errorMessage -> SlackConstants.BlockIds.START_DATE
                SlackConstants.Keywords.END_DATE in errorMessage -> SlackConstants.BlockIds.END_DATE
                SlackConstants.Keywords.SPACE_PASSWORD in errorMessage -> SlackConstants.BlockIds.SPACE_PASSWORD
                SlackConstants.Keywords.PERSONAL_PASSWORD in errorMessage -> SlackConstants.BlockIds.PERSONAL_PASSWORD
                else -> SlackConstants.BlockIds.TEAM_NAME
            }
            val errors = mapOf(blockId to e.message)
            return ctx.ack { it.responseAction("errors").errors(errors)}
        } catch (e: ReservationConflictException) {
            val errors = mapOf(SlackConstants.BlockIds.START_DATE to e.message)
            return ctx.ack { it.responseAction("errors").errors(errors) }
        } catch (e: InvalidReservationException) {
            val errors = mapOf(SlackConstants.BlockIds.START_TIME to e.message)
            return ctx.ack { it.responseAction("errors").errors(errors) }
        } catch (e: PasswordNotMatchException) {
            val errors = mapOf(SlackConstants.BlockIds.SPACE_PASSWORD to e.message)
            return ctx.ack { it.responseAction("errors").errors(errors) }
        } catch (e: InvalidPasswordException) {
            val errors = mapOf(SlackConstants.BlockIds.PERSONAL_PASSWORD to e.message)
            return ctx.ack { it.responseAction("errors").errors(errors) }
        } catch (e: Exception) {
            logger.error("알 수 없는 정기 회의 생성 오류", e)
            val errorView = slackReplyHelper.createErrorView(SlackConstants.Messages.UNKNOWN_ERROR)
            return ctx.ack { it.responseAction("update").view(errorView) }
        }
    }

    private fun createCommandFromModal(req: ViewSubmissionPayload, spaceId: Long): CreateRegularMeetingCommand {
        val values = req.view.state.values

        val teamName = values[SlackConstants.BlockIds.TEAM_NAME]?.get(SlackConstants.ActionIds.TEAM_NAME)?.value
        val dayOfWeekStr = values[SlackConstants.BlockIds.DAY_OF_WEEK]?.get(SlackConstants.ActionIds.DAY_OF_WEEK)?.selectedOption?.value
        val startTimeStr = values[SlackConstants.BlockIds.START_TIME]?.get(SlackConstants.ActionIds.START_TIME)?.selectedOption?.value
        val endTimeStr = values[SlackConstants.BlockIds.END_TIME]?.get(SlackConstants.ActionIds.END_TIME)?.selectedOption?.value
        val startDateStr = values[SlackConstants.BlockIds.START_DATE]?.get(SlackConstants.ActionIds.START_DATE)?.selectedDate
        val endDateStr = values[SlackConstants.BlockIds.END_DATE]?.get(SlackConstants.ActionIds.END_DATE)?.selectedDate
        val spacePassword = values[SlackConstants.BlockIds.SPACE_PASSWORD]?.get(SlackConstants.ActionIds.SPACE_PASSWORD)?.value
        val personalPassword = values[SlackConstants.BlockIds.PERSONAL_PASSWORD]?.get(SlackConstants.ActionIds.PERSONAL_PASSWORD)?.value

        if (teamName.isNullOrBlank()) throw InputParseException("팀 이름을 입력해주세요.")
        if (dayOfWeekStr.isNullOrBlank()) throw InputParseException("요일을 선택해주세요.")
        if (startTimeStr.isNullOrBlank()) throw InputParseException("시작 시간을 선택해주세요.")
        if (endTimeStr.isNullOrBlank()) throw InputParseException("종료 시간을 선택해주세요.")
        if (startDateStr.isNullOrBlank()) throw InputParseException("시작 날짜를 선택해주세요.")
        if (endDateStr.isNullOrBlank()) throw InputParseException("종료 날짜를 선택해주세요.")
        if (spacePassword.isNullOrBlank()) throw InputParseException("공간 비밀번호를 입력해주세요.")
        if (personalPassword.isNullOrBlank()) throw InputParseException("개인 비밀번호를 입력해주세요.")

        val timeRangeStr = "$startTimeStr~$endTimeStr"
        val (startTime, endTime) = inputParser.parseTimeRange(timeRangeStr)

        val dateRangeStr = "$startDateStr~$endDateStr"
        val (startDate, endDate) = inputParser.parseDateRange(dateRangeStr)


        return CreateRegularMeetingCommand(
            spaceId = spaceId,
            teamName = teamName,
            password = spacePassword,
            rawPersonalPassword = personalPassword,
            dayOfWeek = DayOfWeek.valueOf(dayOfWeekStr),
            startTime = startTime,
            endTime = endTime,
            startDate = startDate,
            endDate = endDate
        )
    }

    private fun buildModal(spaceId: String, channelId: String, timeOptions: List<OptionObject>): View {
        val dayOfWeekOptions = DayOfWeek.entries.map {
            BlockCompositions.option(BlockCompositions.plainText(it.getDisplayName(TextStyle.FULL, Locale.KOREAN)), it.name)
        }

        return Views.view { view ->
            view.callbackId(callbackId)
                .privateMetadata("$channelId:$spaceId")
                .type("modal")
                .title(Views.viewTitle { it.type("plain_text").text("정기 회의 정보 입력") })
                .submit(Views.viewSubmit { it.type("plain_text").text("제출") })
                .close(Views.viewClose { it.type("plain_text").text("취소") })
                .blocks(
                    Blocks.asBlocks(
                        Blocks.input {
                            it.blockId(SlackConstants.BlockIds.TEAM_NAME).label(BlockCompositions.plainText(SlackConstants.Keywords.TEAM_NAME))
                                .element(BlockElements.plainTextInput { p -> p.actionId(SlackConstants.ActionIds.TEAM_NAME) })
                        },
                        Blocks.input {
                            it.blockId(SlackConstants.BlockIds.DAY_OF_WEEK).label(BlockCompositions.plainText(SlackConstants.Keywords.DAY_OF_WEEK))
                                .element(BlockElements.staticSelect { s -> s.actionId(SlackConstants.ActionIds.DAY_OF_WEEK).options(dayOfWeekOptions) })
                        },
                        Blocks.input {
                            it.blockId(SlackConstants.BlockIds.START_TIME).label(BlockCompositions.plainText(SlackConstants.Keywords.START_TIME))
                                .element(BlockElements.staticSelect { s -> s.actionId(SlackConstants.ActionIds.START_TIME).options(timeOptions) })
                        },
                        Blocks.input {
                            it.blockId(SlackConstants.BlockIds.END_TIME).label(BlockCompositions.plainText(SlackConstants.Keywords.END_TIME))
                                .element(BlockElements.staticSelect { s -> s.actionId(SlackConstants.ActionIds.END_TIME).options(timeOptions) })
                        },
                        Blocks.input {
                            it.blockId(SlackConstants.BlockIds.START_DATE).label(BlockCompositions.plainText(SlackConstants.Keywords.START_DATE))
                                .element(BlockElements.datePicker { p -> p.actionId(SlackConstants.ActionIds.START_DATE) })
                        },
                        Blocks.input {
                            it.blockId(SlackConstants.BlockIds.END_DATE).label(BlockCompositions.plainText(SlackConstants.Keywords.END_DATE))
                                .element(BlockElements.datePicker { p -> p.actionId(SlackConstants.ActionIds.END_DATE) })
                        },
                        Blocks.input {
                            it.blockId(SlackConstants.BlockIds.SPACE_PASSWORD).label(BlockCompositions.plainText(SlackConstants.Keywords.SPACE_PASSWORD))
                                .element(BlockElements.plainTextInput { p -> p.actionId(SlackConstants.ActionIds.SPACE_PASSWORD) })
                        },
                        Blocks.input {
                            it.blockId(SlackConstants.BlockIds.PERSONAL_PASSWORD).label(BlockCompositions.plainText(SlackConstants.Keywords.PERSONAL_PASSWORD))
                                .element(BlockElements.plainTextInput { p -> p.actionId(SlackConstants.ActionIds.PERSONAL_PASSWORD) })
                        }
                    )
                )
        }
    }
}
