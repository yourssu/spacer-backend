package com.yourssu.spacer.spacehub.implement.domain.discord

import com.yourssu.spacer.spacehub.implement.support.exception.DiscordServerLinkNotFoundException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class DiscordServerLinkReader(
    private val discordServerLinkRepository: DiscordServerLinkRepository
) {
    fun getByDiscordServerId(discordServerId: String): DiscordServerLink? {
        return discordServerLinkRepository.findByDiscordServerId(discordServerId)
            ?: throw DiscordServerLinkNotFoundException("디스코드 서버에 등록된 단체가 아닙니다.")
    }
}