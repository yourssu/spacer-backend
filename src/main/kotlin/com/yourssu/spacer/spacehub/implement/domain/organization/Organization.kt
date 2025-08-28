package com.yourssu.spacer.spacehub.implement.domain.organization

import com.yourssu.spacer.spacehub.implement.support.exception.PasswordNotEncryptedException
import com.yourssu.spacer.spacehub.implement.support.security.password.EncryptPasswordValidator

class Organization(
    val id: Long? = null,
    val email: Email,
    val encryptedPassword: String,
    val name: OrganizationName,
    val logoImageUrl: String,
    val description: String? = null,
    val encryptedReservationPassword: String,
    val hashtags: MutableList<Hashtag> = mutableListOf(),
) {

    init {
        if (EncryptPasswordValidator.isNotEncrypted(encryptedPassword)) {
            throw PasswordNotEncryptedException("비밀번호가 암호화되지 않았습니다.")
        }

        if (EncryptPasswordValidator.isNotEncrypted(encryptedReservationPassword)) {
            throw PasswordNotEncryptedException("예약 비밀번호가 암호화되지 않았습니다.")
        }
    }

    fun updateAndReturnNew(
        email: Email? = this.email,
        encryptedPassword: String? = this.encryptedPassword,
        name: OrganizationName? = this.name,
        logoImageUrl: String? = this.logoImageUrl,
        description: String? = null,
        encryptedReservationPassword: String? = this.encryptedReservationPassword,
        hashtags: List<Hashtag>? = this.hashtags,
    ): Organization {
        return Organization(
            id = this.id,
            email = email ?: this.email,
            encryptedPassword = encryptedPassword ?: this.encryptedPassword,
            name = name ?: this.name,
            logoImageUrl = logoImageUrl ?: this.logoImageUrl,
            description = description,
            encryptedReservationPassword = encryptedReservationPassword ?: this.encryptedReservationPassword,
            hashtags = hashtags?.toMutableList() ?: this.hashtags
        )
    }

    fun updateHashtags(updatedHashtags: List<Hashtag>) {
        hashtags.clear()
        hashtags.addAll(updatedHashtags)
    }

    fun getEmailValue(): String {
        return email.emailAddress
    }

    fun getNameValue(): String {
        return name.name
    }

    fun getHashtagValues(): List<String> = hashtags.map { it.name }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Organization

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "Organization(" +
                "id=$id, " +
                "email=$email, " +
                "encryptedPassword='$encryptedPassword', " +
                "name=$name, " +
                "logoImageUrl=$logoImageUrl, " +
                "description=$description, " +
                "encryptedReservationPassword='$encryptedReservationPassword')"
    }

    fun addHashtags(tags: List<Hashtag>) {
        hashtags.addAll(tags)
    }
}
