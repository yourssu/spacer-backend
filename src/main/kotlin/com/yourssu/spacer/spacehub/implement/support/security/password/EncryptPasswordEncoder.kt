package com.yourssu.spacer.spacehub.implement.support.security.password

import com.yourssu.spacer.spacehub.business.support.exception.PasswordNotMatchException
import com.yourssu.spacer.spacehub.business.support.security.password.PasswordEncoder
import com.yourssu.spacer.spacehub.implement.support.exception.PasswordEncodingFailureException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.Base64
import org.springframework.stereotype.Component

@Component
class EncryptPasswordEncoder : PasswordEncoder {

    companion object {
        private const val VERSION_PREFIX = "$2a" // Spring Security의 BCryptPasswordEncoder에서 사용하는 default 값
        private const val STRENGTH = 10 // Spring Security의 BCryptPasswordEncoder에서 사용하는 default 값
        private val secureRandom = SecureRandom()
        private const val SALT_BYTES_LENGTH = 16
        private const val SALT_OFFSET = 7
        private const val ENCODED_SALT_LENGTH = 22
        private const val HASH_VALUE_BYTES_LENGTH = 24
        private const val ENCODED_HASH_VALUE_LENGTH = 31
    }

    override fun encode(rawPassword: String): String {
        if (rawPassword.isBlank()) {
            throw PasswordEncodingFailureException("비밀번호가 빈 값입니다.")
        }

        val encodedSalt = generateEncodedSalt()
        val encodedHashValue = generateEncodedHashedValue(rawPassword, encodedSalt)

        return formatting(encodedSalt, encodedHashValue)
    }

    private fun generateEncodedSalt(): String {
        val rnd = ByteArray(SALT_BYTES_LENGTH)
        secureRandom.nextBytes(rnd)

        return Base64.getEncoder().withoutPadding().encodeToString(rnd)
    }

    private fun generateEncodedHashedValue(rawPassword: String, encodedSalt: String): String {
        val round = 1 shl STRENGTH
        val decodedSalt = Base64.getDecoder().decode(encodedSalt)
        val hashBytes = hashing(rawPassword, decodedSalt, round)

        val truncatedHashBytes = ByteArray(HASH_VALUE_BYTES_LENGTH)
        System.arraycopy(hashBytes, 0, truncatedHashBytes, 0, HASH_VALUE_BYTES_LENGTH)

        val encodedHashValue = Base64.getEncoder().withoutPadding().encodeToString(truncatedHashBytes)

        return encodedHashValue.substring(0, ENCODED_HASH_VALUE_LENGTH)
    }

    private fun hashing(rawPassword: String, decodedSalt: ByteArray, round: Int): ByteArray {
        val hashAlgorithm = "SHA-256"
        try {
            val md = MessageDigest.getInstance(hashAlgorithm)
            md.update(rawPassword.toByteArray())
            md.update(decodedSalt)
            var hashBytes = md.digest()

            for (i in 0 until round) {
                md.update(hashBytes)
                hashBytes = md.digest()
            }

            return hashBytes
        } catch (e: NoSuchAlgorithmException) {
            throw PasswordEncodingFailureException(hashAlgorithm + "에 해당하는 해시 알고리즘을 찾을 수 없습니다.")
        }
    }

    private fun formatting(encodedSalt: String, encodedHashValue: String): String {
        return String.format("%s$%02d$%s%s", VERSION_PREFIX, STRENGTH, encodedSalt, encodedHashValue)
    }

    override fun matchesOrThrow(rawPassword: String, encodedPassword: String, message: String) {
        if (!matches(rawPassword, encodedPassword)) {
            throw PasswordNotMatchException(message)
        }
    }

    private fun matches(rawPassword: String, encodedPassword: String): Boolean {
        if (EncryptPasswordValidator.isNotEncrypted(encodedPassword)) {
            return false
        }

        val encodedSalt = extractSalt(encodedPassword)
        val encodedHashValue = generateEncodedHashedValue(rawPassword, encodedSalt)
        val expectEncodedPassword = formatting(encodedSalt, encodedHashValue)

        return expectEncodedPassword == encodedPassword
    }

    private fun extractSalt(encodedPassword: String): String {
        return encodedPassword.substring(SALT_OFFSET, SALT_OFFSET + ENCODED_SALT_LENGTH)
    }
}
