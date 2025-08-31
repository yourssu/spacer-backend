package com.yourssu.spacer.spacehub.implement.domain.discord

interface DiscordServerLinkRepository {
    fun findByDiscordServerId(discordServerId: String): DiscordServerLink?
    fun existsByDiscordServerId(discordServerId: String): Boolean
    fun save(discordServerLink: DiscordServerLink): DiscordServerLink
}
