package com.yourssu.spacer.spacehub.business.domain.password

import java.util.regex.Matcher
import java.util.regex.Pattern

class PasswordValidator {

    companion object {

        fun validate(passwordFormat: PasswordFormat, rawPassword: String) {
            validateNotBlank(rawPassword)
            validatePasswordFormat(passwordFormat, rawPassword)
        }

        private fun validateNotBlank(rawPassword: String) {
            if (rawPassword.isBlank()) {
                throw InvalidPasswordException("비밀번호가 빈 값입니다.")
            }
        }

        private fun validatePasswordFormat(format: PasswordFormat, rawPassword: String) {
            val pattern: Pattern = Pattern.compile(format.regex)
            val matcher: Matcher = pattern.matcher(rawPassword)
            if (!matcher.matches()) {
                throw InvalidPasswordException(format.errorMessage)
            }
        }
    }
}
