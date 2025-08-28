package com.yourssu.spacer.spacehub.application.domain.discord

import com.yourssu.spacer.spacehub.business.domain.authentication.AuthenticationService
import com.yourssu.spacer.spacehub.business.domain.authentication.LoginCommand
import com.yourssu.spacer.spacehub.business.domain.discord.DiscordService
import com.yourssu.spacer.spacehub.business.support.exception.PasswordNotMatchException
import com.yourssu.spacer.spacehub.implement.support.exception.DiscordServerLinkNotFoundException
import com.yourssu.spacer.spacehub.implement.support.exception.OrganizationNotFoundException
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class ServerRegistrationHandler(
    private val discordService: DiscordService,
    private val authenticationService: AuthenticationService
) {
    fun handleSlashCommand(event: SlashCommandInteractionEvent) {
        val discordServerId = event.guild!!.id
        try {
            discordService.getOrganizationIdByDiscordServerId(discordServerId)
            event.reply("✅ 이미 연동된 서버입니다!").setEphemeral(true).queue()
        } catch (e: DiscordServerLinkNotFoundException) {
            val modal = Modal.create("create_org_modal:$discordServerId", "서버 등록")
                .addActionRow(
                    TextInput.create("org_email", "이메일", TextInputStyle.SHORT).setRequired(true).build()
                )
                .addActionRow(
                    TextInput.create("org_password", "비밀번호", TextInputStyle.SHORT).setRequired(true).build()
                )
                .build()

            event.replyModal(modal).queue()
        }
    }

    fun handleOrgRegistrationModal(event: ModalInteractionEvent) {
        val discordServerId = event.modalId.split(":")[1]
        val loginCommand = LoginCommand(
            email = event.getValue("org_email")!!.asString,
            password = event.getValue("org_password")!!.asString,
            requestTime = LocalDateTime.now()
        )

        try {
            val organizationId = authenticationService.login(loginCommand).id
            discordService.createServerLink(discordServerId, organizationId)

            event.reply("✅ 디스코드 서버가 SPACER와 연동되었습니다!")
                .setEphemeral(true).queue()

        } catch (e: OrganizationNotFoundException) {
            event.reply("❌ **가입 이력 없음**: 해당 이메일로 가입된 단체를 찾을 수 없습니다.")
                .setEphemeral(true).queue()
        } catch (e: PasswordNotMatchException) {
            event.reply("❌ **비밀번호 불일치**: 비밀번호가 올바르지 않습니다.")
                .setEphemeral(true).queue()
        } catch (e: Exception) {
            event.reply("❌ 알 수 없는 오류가 발생했습니다: ${e.message}")
                .setEphemeral(true).queue()
        }
    }
}