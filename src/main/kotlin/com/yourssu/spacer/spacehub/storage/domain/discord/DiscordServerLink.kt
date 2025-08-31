package com.yourssu.spacer.spacehub.storage.domain.discord

import com.yourssu.spacer.spacehub.implement.domain.discord.DiscordServerLink
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "discord_server_link")
class DiscordServerLinkEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    val discordServerId: String,

    @Column(nullable = false)
    val organizationId: Long
) {
    fun toDomain(): DiscordServerLink {
        return DiscordServerLink(
            id = id,
            discordServerId = discordServerId,
            organizationId = organizationId
        )
    }

    companion object {
        fun from(domain: DiscordServerLink): DiscordServerLinkEntity {
            return DiscordServerLinkEntity(
                id = domain.id,
                discordServerId = domain.discordServerId,
                organizationId = domain.organizationId
            )
        }
    }
}
