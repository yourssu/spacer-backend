package com.yourssu.spacer.spacehub.application.domain.discord

import com.yourssu.spacer.spacehub.business.domain.discord.DiscordService
import com.yourssu.spacer.spacehub.business.domain.space.SpaceService
import com.yourssu.spacer.spacehub.implement.support.exception.DiscordServerLinkNotFoundException
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter

@Component
class DiscordUIFactory(
    private val discordService: DiscordService,
    private val spaceService: SpaceService
) {

    fun getVerifiedOrganizationId(event: SlashCommandInteractionEvent): Long? {
        val discordServerId = event.guild?.id ?: run {
            event.reply("❌ 서버 정보가 없습니다.").setEphemeral(true).queue()
            return null
        }

        return try {
            discordService.getOrganizationIdByDiscordServerId(discordServerId)
        } catch (e: DiscordServerLinkNotFoundException) {
            event.reply("❌ 서버가 단체와 연동되지 않았습니다. `/서버등록` 후 사용해주세요.")
                .setEphemeral(true).queue()
            null
        } catch (e: Exception) {
            event.reply("❌ 알 수 없는 오류가 발생했습니다. 관리자에게 문의해주세요.")
                .setEphemeral(true).queue()
            null
        }
    }

    fun createSpaceSelectMenu(componentId: String, placeholder: String, organizationId: Long): StringSelectMenu {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val spaces = spaceService.readAllByOrganizationId(organizationId).spaceDtos

        return StringSelectMenu.create(componentId)
            .setPlaceholder(placeholder)
            .setRequiredRange(1, 1)
            .addOptions(
                spaces.map { space ->
                    val timeRange = "(${space.openingTime.format(formatter)} ~ ${space.closingTime.format(formatter)})"
                    SelectOption.of("${space.name} $timeRange", space.id.toString())
                }
            )
            .build()
    }
}