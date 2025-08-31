package com.yourssu.spacer.spacehub.application.domain.discord

import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback

fun IReplyCallback.replyError(message: String) {
    this.reply("❌ $message").setEphemeral(true).queue()
}

fun IReplyCallback.replySuccess(message: String) {
    this.reply("✅ $message").setEphemeral(true).queue()
}

fun InteractionHook.sendError(message: String) {
    this.sendMessage("❌ $message").setEphemeral(true).queue()
}

fun InteractionHook.sendSuccess(message: String) {
    this.sendMessage("✅ $message").setEphemeral(true).queue()
}
