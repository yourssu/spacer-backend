package com.yourssu.spacer.spacehub.implement.support.security.password

import java.util.regex.Pattern

class EncryptPasswordValidator {

    companion object {

        private val PATTERN: Pattern = Pattern.compile("^\\$2a\\$\\d{2}\\$\\S{53}$")

        fun isNotEncrypted(password: String): Boolean {
            return (password.isBlank() ||
                    !PATTERN.matcher(password).matches())
        }
    }
}
