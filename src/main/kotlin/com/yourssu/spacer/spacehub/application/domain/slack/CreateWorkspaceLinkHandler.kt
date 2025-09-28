package com.yourssu.spacer.spacehub.application.domain.slack

import com.slack.api.app_backend.views.payload.ViewSubmissionPayload
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
import com.yourssu.spacer.spacehub.business.domain.authentication.AuthenticationService
import com.yourssu.spacer.spacehub.business.domain.authentication.LoginCommand
import com.yourssu.spacer.spacehub.business.support.exception.PasswordNotMatchException
import com.yourssu.spacer.spacehub.implement.domain.slack.SlackWorkspaceLink
import com.yourssu.spacer.spacehub.implement.domain.slack.SlackWorkspaceLinkReader
import com.yourssu.spacer.spacehub.implement.domain.slack.SlackWorkspaceLinkRepository
import com.yourssu.spacer.spacehub.implement.support.exception.OrganizationNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class CreateWorkspaceLinkHandler(
    private val slackWorkspaceLinkRepository: SlackWorkspaceLinkRepository,
    private val slackWorkspaceLinkReader: SlackWorkspaceLinkReader,
    private val authenticationService: AuthenticationService,
    private val slackReplyHelper: SlackReplyHelper
) : SlackSlashHandler, SlackViewSubmissionHandler {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override val command = Commands.WORKSPACE_LINK_CREATE
    override val callbackId = SlackConstants.WORKSPACE_LINK_MODAL_SUBMIT

    override fun handle(req: SlashCommandRequest, ctx: SlashCommandContext): Response {
        val existingLink = slackWorkspaceLinkReader.getByTeamId(ctx.teamId)

        if (existingLink?.organizationId != null) {
            return ctx.ack("✅ 이미 연동된 워크스페이스입니다!")
        }

        try {
            val modalView = buildServerLinkModal(ctx.channelId)
            val botToken = existingLink?.accessToken
            val apiResponse = ctx.client().viewsOpen {
                it.token(botToken)
                    .triggerId(ctx.triggerId)
                    .view(modalView)
            }
            if (!apiResponse.isOk) {
                return ctx.ack(":x: 모달을 여는 중 오류가 발생했습니다: `${apiResponse.error}`")
            }
        } catch (e: Exception) {
            logger.error("워크스페이스 연동 모달 열기 로직에서 예외 발생: teamId=${ctx.teamId}", e)
            return ctx.ack(":x: 모달을 여는 중 내부 서버 오류가 발생했습니다.")
        }
        return ctx.ack()
    }

    override fun handle(req: ViewSubmissionPayload, ctx: ViewSubmissionContext): Response {
        val values = req.view.state.values
        val email = values[SlackConstants.BlockIds.EMAIL]?.get(SlackConstants.ActionIds.EMAIL)?.value ?: ""
        val password = values[SlackConstants.BlockIds.PASSWORD]?.get(SlackConstants.ActionIds.PASSWORD)?.value ?: ""

        val loginCommand = LoginCommand(
            email = email,
            password = password,
            requestTime = LocalDateTime.now()
        )

        try {
            val organizationId = authenticationService.login(loginCommand).id
            val existingLink = slackWorkspaceLinkReader.getByTeamId(ctx.teamId)

            val updatedLink = SlackWorkspaceLink(
                id = existingLink.id,
                teamId = existingLink.teamId,
                accessToken = existingLink.accessToken,
                organizationId = organizationId
            )
            slackWorkspaceLinkRepository.save(updatedLink)

            val successView = Views.view { view ->
                view.type("modal")
                    .title(Views.viewTitle { it.type("plain_text").text("연동 완료").emoji(true) })
                    .close(Views.viewClose { it.type("plain_text").text("확인").emoji(true) })
                    .blocks(
                        Blocks.asBlocks(
                            Blocks.section { section ->
                                section.text(BlockCompositions.markdownText("✅ 워크스페이스가 SPACER와 성공적으로 연동되었습니다!"))
                            }
                        )
                    )
            }

            return ctx.ack { it.responseAction("update").view(successView) }
        } catch (e: OrganizationNotFoundException) {
            val errors = mapOf(SlackConstants.BlockIds.EMAIL to e.message)
            return ctx.ack { it.responseAction("errors").errors(errors) }
        } catch (e: PasswordNotMatchException) {
            val errors = mapOf(SlackConstants.BlockIds.PASSWORD to e.message)
            return ctx.ack { it.responseAction("errors").errors(errors) }
        } catch (e: Exception) {
            logger.error("알 수 없는 워크스페이스 연동 오류", e)
            val errorView = slackReplyHelper.createErrorView(SlackConstants.Messages.UNKNOWN_ERROR)
            return ctx.ack { it.responseAction("update").view(errorView) }
        }
    }

    private fun buildServerLinkModal(channelId: String): View {
        return Views.view { view ->
            view.callbackId(callbackId)
                .privateMetadata(channelId)
                .type("modal")
                .title(Views.viewTitle { it.type("plain_text").text("워크스페이스 등록") })
                .submit(Views.viewSubmit { it.type("plain_text").text("등록") })
                .close(Views.viewClose { it.type("plain_text").text("취소") })
                .blocks(
                    Blocks.asBlocks(
                        Blocks.input { input ->
                            input.blockId(SlackConstants.BlockIds.EMAIL)
                                .label(BlockCompositions.plainText(SlackConstants.Keywords.EMAIL))
                            input.element(BlockElements.emailTextInput { it.actionId(SlackConstants.ActionIds.EMAIL) })
                        },
                        Blocks.input { input ->
                            input.blockId(SlackConstants.BlockIds.PASSWORD)
                                .label(BlockCompositions.plainText(SlackConstants.Keywords.PASSWORD))
                            input.element(BlockElements.plainTextInput { it.actionId(SlackConstants.ActionIds.PASSWORD) })
                        }
                    )
                )
        }
    }
}
