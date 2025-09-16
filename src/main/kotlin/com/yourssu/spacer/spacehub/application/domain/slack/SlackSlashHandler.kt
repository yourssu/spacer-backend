package com.yourssu.spacer.spacehub.application.domain.slack

import com.slack.api.bolt.context.builtin.SlashCommandContext
import com.slack.api.bolt.request.builtin.SlashCommandRequest
import com.slack.api.bolt.response.Response

interface SlackSlashHandler {
    val command: String
    fun handle(req: SlashCommandRequest, ctx: SlashCommandContext): Response
}
