package com.yourssu.spacer.spacehub.application.domain.slack

import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload
import com.slack.api.bolt.context.builtin.ActionContext
import com.slack.api.bolt.response.Response

interface SlackBlockActionHandler {
    val actionId: String
    fun handle(req: BlockActionPayload, ctx: ActionContext): Response
}