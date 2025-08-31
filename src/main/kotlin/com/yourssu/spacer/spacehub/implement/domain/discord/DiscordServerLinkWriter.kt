package com.yourssu.spacer.spacehub.implement.domain.discord

import com.yourssu.spacer.spacehub.implement.support.exception.DiscordServerLinkConflictException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class DiscordServerLinkWriter(
    private val discordServerLinkRepository: DiscordServerLinkRepository
) {

    fun write(discordServerLink: DiscordServerLink): DiscordServerLink {
        if (discordServerLinkRepository.existsByDiscordServerId(discordServerLink.discordServerId)) {
            throw DiscordServerLinkConflictException("Discord 서버에 이미 등록된 단체입니다.")
        }
        return discordServerLinkRepository.save(discordServerLink)
    }
}