package com.yourssu.spacer.spacehub.implement.support.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "discord.bot")
data class DiscordProperties(
    val token: String
)