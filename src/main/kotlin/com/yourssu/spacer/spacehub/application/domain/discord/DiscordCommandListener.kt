package com.yourssu.spacer.spacehub.application.domain.discord

import com.yourssu.spacer.spacehub.application.support.constants.DiscordConstants
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.stereotype.Component

@Component
class DiscordCommandListener(
    private val createServerLinkHandler: CreateServerLinkHandler,
    private val createReservationHandler: CreateReservationHandler,
    private val readReservationHandler: ReadReservationHandler,
    private val deleteReservationHandler: DeleteReservationHandler
) : ListenerAdapter() {

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        when (event.name) {
            "서버등록" -> createServerLinkHandler.handleSlashCommand(event)
            "동방예약" -> createReservationHandler.handleSlashCommand(event)
            "예약조회" -> readReservationHandler.handleSlashCommand(event)
        }
    }

    override fun onStringSelectInteraction(event: StringSelectInteractionEvent) {
        when {
            event.componentId.startsWith(DiscordConstants.RESERVATION_CREATE_SPACE_SELECT) ->
                createReservationHandler.handleSelectMenu(event)
            event.componentId.startsWith(DiscordConstants.RESERVATION_READ_SPACE_SELECT) ->
                readReservationHandler.handleSelectMenu(event)
            event.componentId.startsWith(DiscordConstants.RESERVATION_DELETE_RESERVATION_SELECT) ->
                deleteReservationHandler.handleSelectMenu(event)
        }
    }

    override fun onModalInteraction(event: ModalInteractionEvent) {
        when {
            event.modalId.startsWith(DiscordConstants.SERVER_LINK_CREATE_MODAL) ->
                createServerLinkHandler.handleCreateModal(event)
            event.modalId.startsWith(DiscordConstants.RESERVATION_CREATE_MODAL) ->
                createReservationHandler.handleCreateModal(event)
            event.modalId.startsWith(DiscordConstants.RESERVATION_READ_MODAL) ->
                readReservationHandler.handleReadModal(event)
            event.modalId.startsWith(DiscordConstants.RESERVATION_DELETE_MODAL) ->
                deleteReservationHandler.handleDeleteModal(event)
        }
    }
}