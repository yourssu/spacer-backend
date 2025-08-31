package com.yourssu.spacer.spacehub.implement.support.exception

class DiscordServerLinkConflictException (
    override val message: String
) : RuntimeException(message)