package com.yourssu.spacer.spacehub.implement.domain.organization

import com.yourssu.spacer.spacehub.business.domain.organization.CreateOrganizationCommand
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class OrganizationWriter(
    private val organizationRepository: OrganizationRepository,
    private val hashtagWriter: HashtagWriter,
) {

    fun write(
        command: CreateOrganizationCommand,
        logoImageUrl: String,
        encryptedPassword: String,
        encryptedReservationPassword: String,
    ): Organization {
        val toSave = Organization(
            email = Email(command.email),
            encryptedPassword = encryptedPassword,
            name = OrganizationName(command.name),
            logoImageUrl = logoImageUrl,
            description = command.description,
            encryptedReservationPassword = encryptedReservationPassword,
        )
        val savedOrganization: Organization = organizationRepository.save(toSave)
        val savedHashtags: List<Hashtag> = hashtagWriter.write(savedOrganization.id!!, command.hashtags)

        savedOrganization.addHashtags(savedHashtags)

        return savedOrganization
    }

    fun update(toUpdate: Organization, hashtags: List<String>): Organization {
        val updatedOrganization: Organization = organizationRepository.save(toUpdate)
        val updatedHashtags: List<Hashtag> = hashtagWriter.update(updatedOrganization, hashtags)

        updatedOrganization.updateHashtags(updatedHashtags)

        return updatedOrganization
    }

    fun withdraw(organizationId: Long) {
        organizationRepository.deleteById(organizationId)
    }
}
