package com.yourssu.spacer.spacehub.application.domain.slack

import com.slack.api.bolt.context.builtin.ViewSubmissionContext
import com.slack.api.model.block.Blocks
import com.slack.api.model.block.composition.BlockCompositions
import com.slack.api.model.view.View
import com.slack.api.model.view.Views
import com.yourssu.spacer.spacehub.implement.domain.slack.SlackWorkspaceLinkReader
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SlackReplyHelper(
    private val slackWorkspaceLinkReader: SlackWorkspaceLinkReader
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun createErrorView(message: String): View {
        return Views.view { view ->
            view.type("modal")
                .title(Views.viewTitle { it.type("plain_text").text("오류 발생").emoji(true) })
                .close(Views.viewClose { it.type("plain_text").text("닫기").emoji(true) })
                .blocks(Blocks.asBlocks(Blocks.section { it.text(BlockCompositions.markdownText(":x: $message")) }))
        }
    }

    fun sendSuccess(ctx: ViewSubmissionContext, channelId: String, message: String) {
        sendEphemeralMessage(ctx, channelId, "✅ $message")
    }

    fun sendError(ctx: ViewSubmissionContext, channelId: String, message: String) {
        sendEphemeralMessage(ctx, channelId, "❌ $message")
    }

    private fun sendEphemeralMessage(ctx: ViewSubmissionContext, channelId: String, text: String) {
        try {
            val botToken = slackWorkspaceLinkReader.getByTeamId(ctx.teamId).accessToken

            val response = ctx.client().chatPostEphemeral {
                it.token(botToken)
                    .channel(channelId)
                    .user(ctx.requestUserId)
                    .text(text)
            }
            if (!response.isOk) {
                logger.error(
                    "Ephemeral 메시지 전송 실패: teamId={}, userId={}, channelId={}, error={}",
                    ctx.teamId,
                    ctx.requestUserId,
                    channelId,
                    response.error
                )
            }
        } catch (e: Exception) {
            logger.error(
                "Ephemeral 메시지 전송 중 예외 발생: teamId={}, userId={}, channelId={}",
                ctx.teamId,
                ctx.requestUserId,
                channelId,
                e
            )
        }
    }
}
