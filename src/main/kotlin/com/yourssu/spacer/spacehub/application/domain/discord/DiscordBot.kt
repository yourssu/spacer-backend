package com.yourssu.spacer.spacehub.application.domain.discord

import com.yourssu.spacer.spacehub.application.support.configuration.DiscordProperties
import com.yourssu.spacer.spacehub.application.support.constants.SlashCommands
import jakarta.annotation.PostConstruct
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.Commands
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
        jda.updateCommands().addCommands(
            Commands.slash(SlashCommands.SERVER_LINK_CREATE, "해당 디스코드 서버를 SPACER와 연결합니다.")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER)),
            Commands.slash(SlashCommands.RESERVATION_CREATE, "공간을 예약합니다."),
            Commands.slash(SlashCommands.RESERVATION_READ, "특정 날짜의 예약 현황을 조회합니다."),
            Commands.slash(SlashCommands.RESERVATION_DELETE, "특정 날짜의 예약 정보를 취소합니다."),
            Commands.slash(SlashCommands.REGULAR_MEETING_CREATE, "정기 회의 등록 후, 지정한 요일 및 시간에 정기적으로 공간을 예약합니다."),
            Commands.slash(SlashCommands.REGULAR_MEETING_READ, "현재 진행 중인 정기 회의 목록을 조회합니다."),
            Commands.slash(SlashCommands.REGULAR_MEETING_DELETE, "특정 정기 회의를 취소합니다.")
        ).queue()
    }
}
