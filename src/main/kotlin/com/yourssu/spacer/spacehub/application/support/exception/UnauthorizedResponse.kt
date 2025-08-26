package com.yourssu.spacer.spacehub.application.support.exception

data class UnauthorizedResponse(
    val message: String,
    val refreshRequired: Boolean
)
