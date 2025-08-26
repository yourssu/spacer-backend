package com.yourssu.spacer.spacehub.application.support.authentication

class LoginRequiredException(
    override val message: String
) : RuntimeException(message)
