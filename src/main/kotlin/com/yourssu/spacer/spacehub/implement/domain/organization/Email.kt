package com.yourssu.spacer.spacehub.implement.domain.organization

import com.yourssu.spacer.spacehub.implement.support.exception.InvalidEmailException
import java.util.regex.Matcher
import java.util.regex.Pattern

class Email(
    val emailAddress: String,
) {

    companion object {
        private const val EMAIL_REGEX = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
    }

    init {
        validateNotBlank(emailAddress)
        validateEmailFormat(emailAddress)
    }

    private fun validateNotBlank(emailAddress: String) {
        if (emailAddress.isBlank()) {
            throw InvalidEmailException("email 주소가 빈 값입니다.")
        }
    }

    private fun validateEmailFormat(emailAddress: String) {
        val pattern: Pattern = Pattern.compile(EMAIL_REGEX)
        val matcher: Matcher = pattern.matcher(emailAddress)
        if (!matcher.matches()) {
            throw InvalidEmailException("유효하지 않은 email 형식입니다.")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Email

        return emailAddress == other.emailAddress
    }

    override fun hashCode(): Int {
        return emailAddress.hashCode()
    }

    override fun toString(): String {
        return "Email(emailAddress='$emailAddress')"
    }
}
