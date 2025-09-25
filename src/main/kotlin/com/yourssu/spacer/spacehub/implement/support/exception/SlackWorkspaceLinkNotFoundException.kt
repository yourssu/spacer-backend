package com.yourssu.spacer.spacehub.implement.support.exception

class SlackWorkspaceLinkNotFoundException (
    override val message: String
) : RuntimeException(message)