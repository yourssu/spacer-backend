package com.yourssu.spacer.spacehub.application.domain.slack

import com.slack.api.bolt.context.builtin.ViewSubmissionContext
import com.yourssu.spacer.spacehub.implement.domain.slack.SlackWorkspaceLinkReader
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SlackReplyHelper(
    private val slackWorkspaceLinkReader: SlackWorkspaceLinkReader
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

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
