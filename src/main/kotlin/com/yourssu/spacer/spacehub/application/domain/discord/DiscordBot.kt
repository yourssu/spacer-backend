package com.yourssu.spacer.spacehub.application.domain.discord

import com.yourssu.spacer.spacehub.application.support.configuration.DiscordProperties
import jakarta.annotation.PostConstruct
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
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
            .addEventListeners(commandListener)
            .build()
            .awaitReady()
        registerCommands()
    }

    private fun registerCommands() {
        jda.upsertCommand("서버등록", "해당 디스코드 서버를 SPACER와 연결합니다.")
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
            .queue()
        jda.upsertCommand("동방예약", "공간을 예약합니다.").queue()
        jda.upsertCommand("예약조회", "특정 날짜의 예약 현황을 조회합니다.").queue()
        jda.upsertCommand("정기회의", "요일, 시간을 지정하여 정기적으로 공간을 예약합니다.").queue()
    }
}
