package com.yourssu.spacer.spacehub.application.domain.slack

import com.slack.api.app_backend.views.payload.ViewSubmissionPayload
import com.slack.api.bolt.context.builtin.ViewSubmissionContext
import com.slack.api.bolt.response.Response

interface SlackViewSubmissionHandler {
    val callbackId: String
    fun handle(req: ViewSubmissionPayload, ctx: ViewSubmissionContext): Response
}
