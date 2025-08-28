package com.yourssu.spacer.spacehub.storage.domain.discord

import org.springframework.data.jpa.repository.JpaRepository

interface JpaDiscordServerLinkRepository : JpaRepository<DiscordServerLinkEntity, Long> {

    fun findByDiscordServerId(discordServerId: String): DiscordServerLinkEntity?
    fun existsByDiscordServerId(discordServerId: String): Boolean
}
