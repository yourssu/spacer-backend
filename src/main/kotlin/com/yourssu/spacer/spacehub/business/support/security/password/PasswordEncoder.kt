package com.yourssu.spacer.spacehub.business.support.security.password

interface PasswordEncoder {

    fun encode(rawPassword: String): String
    fun matchesOrThrow(rawPassword: String, encodedPassword: String, message: String)
}
