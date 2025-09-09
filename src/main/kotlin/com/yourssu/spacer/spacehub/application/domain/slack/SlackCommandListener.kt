package com.yourssu.spacer.spacehub.application.domain.slack

import com.slack.api.bolt.App
import org.springframework.stereotype.Component

@Component
class SlackCommandListener(
    slashHandlers: List<SlackSlashHandler>,
    viewSubmissionHandlers: List<SlackViewSubmissionHandler>,
    blockActionHandlers: List<SlackBlockActionHandler>
) {
    private val slashHandlerMap = slashHandlers.associateBy { it.command }
    private val viewSubmissionHandlerMap = viewSubmissionHandlers.associateBy { it.callbackId }
    private val blockActionHandlerMap = blockActionHandlers.associateBy { it.actionId }

    fun applyTo(app: App) {
        slashHandlerMap.forEach { (command, handler) ->
            app.command(command) { req, ctx -> handler.handle(req, ctx) }
        }
        viewSubmissionHandlerMap.forEach { (callbackId, handler) ->
            app.viewSubmission(callbackId) { req, ctx ->
                // 콜백 ID가 동적으로 변할 수 있으므로 startsWith로 매칭
                if (req.payload.view.callbackId.startsWith(callbackId)) {
                    handler.handle(req.payload, ctx)
                } else {
                    ctx.ack()
                }
            }
        }
        blockActionHandlerMap.forEach { (actionId, handler) ->
            app.blockAction(actionId) { req, ctx -> handler.handle(req.payload, ctx) }
        }
    }
}
