package com.yourssu.spacer.spacehub.application.domain.discord

import com.yourssu.spacer.spacehub.application.support.constants.DiscordConstants
import com.yourssu.spacer.spacehub.application.support.constants.SlashCommands
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
    private val deleteReservationHandler: DeleteReservationHandler,
    private val createRegularMeetingHandler: CreateRegularMeetingHandler,
    private val readRegularMeetingHandler: ReadRegularMeetingHandler,
    private val deleteRegularMeetingHandler: DeleteRegularMeetingHandler
) : ListenerAdapter() {

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        when (event.name) {
            SlashCommands.SERVER_LINK_CREATE -> createServerLinkHandler.handleSlashCommand(event)
            SlashCommands.RESERVATION_CREATE -> createReservationHandler.handleSlashCommand(event)
            SlashCommands.RESERVATION_READ -> readReservationHandler.handleSlashCommand(event)
            SlashCommands.RESERVATION_DELETE -> deleteReservationHandler.handleSlashCommand(event)
            SlashCommands.REGULAR_MEETING_CREATE -> createRegularMeetingHandler.handleSlashCommand(event)
            SlashCommands.REGULAR_MEETING_READ -> readRegularMeetingHandler.handleSlashCommand(event)
            SlashCommands.REGULAR_MEETING_DELETE -> deleteRegularMeetingHandler.handleSlashCommand(event)
        }
    }

    override fun onStringSelectInteraction(event: StringSelectInteractionEvent) {
        when {
            event.componentId.startsWith(DiscordConstants.RESERVATION_CREATE_SPACE_SELECT) ->
                createReservationHandler.handleSelectMenu(event)
            event.componentId.startsWith(DiscordConstants.RESERVATION_READ_SPACE_SELECT) ->
                readReservationHandler.handleSelectMenu(event)
            event.componentId.startsWith(DiscordConstants.RESERVATION_DELETE_SPACE_SELECT) ->
                deleteReservationHandler.handleSelectMenu(event)
            event.componentId.startsWith(DiscordConstants.RECURRING_RESERVATION_CREATE_SPACE_SELECT) ->
                createRegularMeetingHandler.handleSelectMenu(event)
            event.componentId.startsWith(DiscordConstants.REGULAR_MEETING_READ_SPACE_SELECT) ->
                readRegularMeetingHandler.handleSelectMenu(event)
            event.componentId.startsWith(DiscordConstants.REGULAR_MEETING_DELETE_SPACE_SELECT) ->
                deleteRegularMeetingHandler.handleSelectMenu(event)
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
            event.modalId.startsWith(DiscordConstants.RECURRING_RESERVATION_CREATE_MODAL) ->
                createRegularMeetingHandler.handleCreateModal(event)
            event.modalId.startsWith(DiscordConstants.REGULAR_MEETING_DELETE_MODAL) ->
                deleteRegularMeetingHandler.handleDeleteModal(event)
        }
    }
}
