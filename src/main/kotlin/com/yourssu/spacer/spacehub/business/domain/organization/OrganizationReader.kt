package com.yourssu.spacer.spacehub.business.domain.organization

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class OrganizationReader(
    private val organizationRepository: OrganizationRepository,
    private val hashtagReader: HashtagReader
) {

    fun existByEmail(email: String): Boolean {
        return organizationRepository.existsByEmail(email)
    }

    fun getByEmail(email: String): Organization {
        val savedOrganization = (organizationRepository.findByEmail(email)
            ?: throw OrganizationNotFoundException("$email 로 가입한 이력이 없습니다."))

        val hashtags: List<Hashtag> = hashtagReader.readAllByOrganizationId(savedOrganization.id!!)

        savedOrganization.addHashtags(hashtags)

        return savedOrganization
    }

    fun searchByNameKeyword(keyword: String): List<Organization> {
        val savedOrganizations: List<Organization> = organizationRepository.searchByNameKeyword(keyword)
        for (savedOrganization in savedOrganizations) {
            val hashtags: List<Hashtag> = hashtagReader.readAllByOrganizationId(savedOrganization.id!!)
            savedOrganization.addHashtags(hashtags)
        }

        return savedOrganizations
    }

    fun getById(id: Long): Organization {
        val savedOrganization: Organization = (organizationRepository.findById(id)
            ?: throw OrganizationNotFoundException("지정한 단체를 찾을 수 없습니다."))

        val hashtags: List<Hashtag> = hashtagReader.readAllByOrganizationId(savedOrganization.id!!)

        savedOrganization.addHashtags(hashtags)

        return savedOrganization
    }

    fun existsById(organizationId: Long): Boolean {
        return organizationRepository.existsById(organizationId)
    }
}
