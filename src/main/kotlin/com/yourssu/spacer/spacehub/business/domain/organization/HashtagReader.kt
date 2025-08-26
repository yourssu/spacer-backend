package com.yourssu.spacer.spacehub.business.domain.organization

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class HashtagReader(
    private val organizationHashtagRepository: OrganizationHashtagRepository,
    private val hashtagRepository: HashtagRepository,
) {

    fun readAllByOrganizationId(organizationId: Long) =
        organizationHashtagRepository.findAllByOrganizationId(organizationId)
            .map { hashtag -> hashtag.hashtagId }
            .map { hashtagId -> hashtagRepository.findById(hashtagId)!! }
}
