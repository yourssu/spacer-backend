package com.yourssu.spacer.spacehub.application.domain.discord

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.stereotype.Component

@Component
class DiscordCommandListener(
    private val serverRegistrationHandler: ServerRegistrationHandler,
    private val reservationHandler: ReservationHandler
) : ListenerAdapter() {

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        when (event.name) {
            "서버등록" -> serverRegistrationHandler.handleSlashCommand(event)
            "예약하기" -> reservationHandler.handleSlashCommand(event)
        }
    }

    override fun onStringSelectInteraction(event: StringSelectInteractionEvent) {
        reservationHandler.handleSelectMenu(event)
    }

    override fun onModalInteraction(event: ModalInteractionEvent) {
        when {
            event.modalId.startsWith("reservation_modal") ->
                reservationHandler.handleReservationModal(event)
            event.modalId.startsWith("create_org_modal") ->
                serverRegistrationHandler.handleOrgRegistrationModal(event)
        }
    }
}