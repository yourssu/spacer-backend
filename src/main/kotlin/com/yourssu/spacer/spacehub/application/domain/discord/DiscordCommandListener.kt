package com.yourssu.spacer.spacehub.application.domain.discord

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.stereotype.Component

@Component
class DiscordCommandListener(
    private val serverRegistrationHandler: ServerRegistrationHandler,
    private val reservationCreationHandler: ReservationCreationHandler,
    private val reservationReadHandler: ReservationReadHandler
) : ListenerAdapter() {

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        when (event.name) {
            "서버등록" -> serverRegistrationHandler.handleSlashCommand(event)
            "동방예약" -> reservationCreationHandler.handleSlashCommand(event)
            "예약조회" -> reservationReadHandler.handleSlashCommand(event)
        }
    }

    override fun onStringSelectInteraction(event: StringSelectInteractionEvent) {
        when {
            event.componentId.startsWith("space_select_create") -> reservationCreationHandler.handleSelectMenu(event)
            event.componentId.startsWith("space_select_read") -> reservationReadHandler.handleSelectMenu(event)
        }
    }

    override fun onModalInteraction(event: ModalInteractionEvent) {
        when {
            event.modalId.startsWith("create_org_modal") ->
                serverRegistrationHandler.handleOrgRegistrationModal(event)
            event.modalId.startsWith("create_reservation_modal") ->
                reservationCreationHandler.handleReservationModal(event)
            event.modalId.startsWith("read_reservation_modal") ->
                reservationReadHandler.handleReadModal(event)
        }
    }
}