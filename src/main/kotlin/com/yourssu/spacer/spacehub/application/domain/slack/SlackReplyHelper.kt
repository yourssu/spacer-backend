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

    fun sendSuccess(ctx: ViewSubmissionContext, message: String) {
        sendDirectMessage(ctx, "✅ $message")
    }

    fun sendError(ctx: ViewSubmissionContext, message: String) {
        sendDirectMessage(ctx, "❌ $message")
    }

    private fun sendDirectMessage(ctx: ViewSubmissionContext, text: String) {
        try {
            val botToken = slackWorkspaceLinkReader.getByTeamId(ctx.teamId).accessToken

            val conversation = ctx.client().conversationsOpen {
                it.token(botToken)
                    .users(listOf(ctx.requestUserId))
            }

            if (conversation.isOk) {
                val channelId = conversation.channel.id
                val response = ctx.client().chatPostMessage {
                    it.token(botToken)
                        .channel(channelId)
                        .text(text)
                }
                if (!response.isOk) {
                    logger.error(
                        "DM 메시지 전송 실패: teamId={}, userId={}, error={}",
                        ctx.teamId,
                        ctx.requestUserId,
                        response.error
                    )
                }
            } else {
                logger.error(
                    "DM 채널 열기 실패: teamId={}, userId={}, error={}",
                    ctx.teamId,
                    ctx.requestUserId,
                    conversation.error
                )
            }
        } catch (e: Exception) {
            logger.error(
                "DM 메시지 전송 중 예외 발생: teamId={}, userId={}",
                ctx.teamId,
                ctx.requestUserId,
                e
            )
        }
    }
}
