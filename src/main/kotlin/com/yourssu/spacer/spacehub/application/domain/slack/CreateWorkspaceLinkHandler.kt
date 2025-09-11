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
        val existingLink = slackWorkspaceLinkRepository.findByTeamId(ctx.teamId)

        if (existingLink != null && existingLink.organizationId != null) {
            return ctx.ack("✅ 이미 연동된 워크스페이스입니다!")
        }

        try {
            val modalView = buildServerLinkModal()
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
        val loginCommand = LoginCommand(
            email = req.view.state.values["email_block"]?.get("email_input")?.value ?: "",
            password = req.view.state.values["password_block"]?.get("password_input")?.value ?: "",
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

            slackReplyHelper.sendSuccess(ctx, "워크스페이스가 SPACER와 성공적으로 연동되었습니다!")
        } catch (e: OrganizationNotFoundException) {
            slackReplyHelper.sendError(ctx, "가입 이력 없음: + ${e.message}")
        } catch (e: PasswordNotMatchException) {
            slackReplyHelper.sendError(ctx, "비밀번호 불일치: + ${e.message}")
        } catch (e: Exception) {
            slackReplyHelper.sendError(ctx, "알 수 없는 오류가 발생했습니다. 관리자에게 문의하세요.")
            logger.error("알 수 없는 워크스페이스 연동 오류", e)
        }
        return ctx.ack()
    }

    private fun buildServerLinkModal(): View {
        return Views.view { view ->
            view.callbackId(callbackId)
                .type("modal")
                .title(Views.viewTitle { it.type("plain_text").text("워크스페이스 등록") })
                .submit(Views.viewSubmit { it.type("plain_text").text("등록") })
                .close(Views.viewClose { it.type("plain_text").text("취소") })
                .blocks(
                    Blocks.asBlocks(
                        Blocks.input { input ->
                            input.blockId("email_block").label(BlockCompositions.plainText("이메일"))
                            input.element(BlockElements.emailTextInput { it.actionId("email_input") })
                        },
                        Blocks.input { input ->
                            input.blockId("password_block").label(BlockCompositions.plainText("비밀번호"))
                            input.element(BlockElements.plainTextInput { it.actionId("password_input") })
                        }
                    )
                )
        }
    }
}
