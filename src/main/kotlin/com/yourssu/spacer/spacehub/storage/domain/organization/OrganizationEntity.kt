package com.yourssu.spacer.spacehub.storage.domain.organization

import com.yourssu.spacer.spacehub.implement.domain.organization.Email
import com.yourssu.spacer.spacehub.implement.domain.organization.Organization
import com.yourssu.spacer.spacehub.implement.domain.organization.OrganizationName
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "organization")
class OrganizationEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(nullable = false)
    val encryptedPassword: String,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val logoImageUrl: String,

    @Column
    val description: String? = null,

    @Column(nullable = false)
    val encryptedReservationPassword: String,
) {

    companion object {
        fun from(organization: Organization) = OrganizationEntity(
            id = organization.id,
            email = organization.getEmailValue(),
            encryptedPassword = organization.encryptedPassword,
            name = organization.getNameValue(),
            logoImageUrl = organization.logoImageUrl,
            description = organization.description,
            encryptedReservationPassword = organization.encryptedReservationPassword,
        )
    }

    fun toDomain() = Organization(
        id = id,
        email = Email(email),
        encryptedPassword = encryptedPassword,
        name = OrganizationName(name),
        logoImageUrl = logoImageUrl,
        description = description,
        encryptedReservationPassword = encryptedReservationPassword,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OrganizationEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "OrganizationEntity(" +
                "id=$id, " +
                "email='$email', " +
                "encryptedPassword='$encryptedPassword', " +
                "name='$name', " +
                "logoImageUrl=$logoImageUrl, " +
                "description=$description, " +
                "encryptedReservationPassword='$encryptedReservationPassword')"
    }
}
