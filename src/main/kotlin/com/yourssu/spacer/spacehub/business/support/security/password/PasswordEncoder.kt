package com.yourssu.spacer.spacehub.business.support.security.password

interface PasswordEncoder {

    fun encode(rawPassword: String): String
    fun matches(rawPassword: String, encodedPassword: String): Boolean
}
