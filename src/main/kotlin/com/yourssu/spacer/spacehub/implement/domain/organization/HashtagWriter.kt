package com.yourssu.spacer.spacehub.implement.domain.organization

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class HashtagWriter(
    private val hashtagRepository: HashtagRepository,
    private val organizationHashtagRepository: OrganizationHashtagRepository,
) {

    fun write(
        organizationId: Long,
        hashtagNames: List<String>,
    ): List<Hashtag> {
        val savedHashtags: MutableList<Hashtag> = mutableListOf()
        for (hashtagName in hashtagNames) {
            val savedHashtag: Hashtag = hashtagRepository.findByName(hashtagName)
                ?: hashtagRepository.save(Hashtag(name = hashtagName))

            organizationHashtagRepository.findByOrganizationIdAndHashtagId(organizationId, savedHashtag.id!!)
                ?: organizationHashtagRepository.save(
                    OrganizationHashtag(
                        organizationId = organizationId,
                        hashtagId = savedHashtag.id
                    )
                )

            savedHashtags.add(savedHashtag)
        }

        return savedHashtags
    }

    fun update(organization: Organization, hashtagNames: List<String>): List<Hashtag> {
        val organizationId: Long = organization.id!!
        organizationHashtagRepository.deleteAllByOrganizationId(organizationId)

        return write(organizationId, hashtagNames)
    }
}
