package com.yourssu.spacer.spacehub.business.domain.discord

import com.yourssu.spacer.spacehub.business.domain.authentication.AuthenticationService
import com.yourssu.spacer.spacehub.business.domain.authentication.LoginCommand
import com.yourssu.spacer.spacehub.business.domain.reservation.ReservationService
import com.yourssu.spacer.spacehub.implement.domain.discord.DiscordServerLink
import com.yourssu.spacer.spacehub.implement.domain.discord.DiscordServerLinkReader
import com.yourssu.spacer.spacehub.implement.domain.discord.DiscordServerLinkWriter
import com.yourssu.spacer.spacehub.implement.domain.organization.Organization
import com.yourssu.spacer.spacehub.implement.domain.organization.OrganizationReader
import org.springframework.stereotype.Service

@Service
class DiscordService (
    private val discordServerLinkReader: DiscordServerLinkReader,
    private val discordServerLinkWriter: DiscordServerLinkWriter,
) {
    fun createServerLink(discordServerId: String, organizationId: Long) {
        val accountLink = DiscordServerLink(
            discordServerId = discordServerId,
            organizationId = organizationId
        )
        discordServerLinkWriter.write(accountLink)
    }

    fun getOrganizationIdByDiscordServerId(discordServerId: String): Long {
        return discordServerLinkReader.getByDiscordServerId(discordServerId).organizationId
    }
}