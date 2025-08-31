package com.yourssu.spacer.spacehub.implement.support.exception

class DiscordServerLinkNotFoundException (
    override val message: String
) : RuntimeException(message)