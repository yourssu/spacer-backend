package com.yourssu.spacer.spacehub.application.domain.discord

import com.yourssu.spacer.spacehub.implement.support.configuration.DiscordProperties
import jakarta.annotation.PostConstruct
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.requests.GatewayIntent
import org.springframework.stereotype.Component

@Component
class DiscordBot(
    private val properties: DiscordProperties,
    private val commandListener: DiscordCommandListener
) {
    lateinit var jda: JDA

    @PostConstruct
    fun init() {
        jda = JDABuilder.createDefault(properties.token)
            .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
            .addEventListeners(commandListener)
            .build()
            .awaitReady()

        registerCommands()
    }

    private fun registerCommands() {
        jda.upsertCommand("서버등록", "해당 디스코드 서버를 SPACER와 연결합니다.")
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)) // 👈 관리자만 사용 가능하도록 설정
            .queue()

        jda.upsertCommand("예약하기", "공간을 예약합니다.").queue()
    }
}
