package com.yourssu.spacer.spacehub.storage.domain.discord

import com.yourssu.spacer.spacehub.implement.domain.discord.DiscordServerLink
import com.yourssu.spacer.spacehub.implement.domain.discord.DiscordServerLinkRepository
import org.springframework.stereotype.Repository

@Repository
class DiscordServerLinkRepositoryImpl(
    private val jpaDiscordServerLinkRepository: JpaDiscordServerLinkRepository
) : DiscordServerLinkRepository {

    override fun findByDiscordServerId(discordServerId: String): DiscordServerLink? {
        return jpaDiscordServerLinkRepository.findByDiscordServerId(discordServerId)?.toDomain()
    }

    override fun existsByDiscordServerId(discordServerId: String): Boolean {
        return jpaDiscordServerLinkRepository.existsByDiscordServerId(discordServerId)
    }

    override fun save(discordServerLink: DiscordServerLink): DiscordServerLink {
        return jpaDiscordServerLinkRepository.save(DiscordServerLinkEntity.from(discordServerLink)).toDomain()
    }
}
