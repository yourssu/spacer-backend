package com.yourssu.spacer.spacehub.storage.domain.organization

import com.yourssu.spacer.spacehub.business.domain.organization.Organization
import com.yourssu.spacer.spacehub.business.domain.organization.OrganizationRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class OrganizationRepositoryImpl(
    private val jpaOrganizationRepository: JpaOrganizationRepository,
) : OrganizationRepository {

    override fun save(organization: Organization): Organization {
        return jpaOrganizationRepository.save(OrganizationEntity.from(organization)).toDomain()
    }

    override fun existsById(id: Long): Boolean {
        return jpaOrganizationRepository.existsById(id)
    }

    override fun existsByEmail(email: String): Boolean {
        return jpaOrganizationRepository.existsByEmail(email)
    }

    override fun findById(id: Long): Organization? {
        return jpaOrganizationRepository.findByIdOrNull(id)?.toDomain()
    }

    override fun findByEmail(email: String): Organization? {
        return jpaOrganizationRepository.findByEmail(email)?.toDomain()
    }

    override fun searchByNameKeyword(keyword: String): List<Organization> {
        return jpaOrganizationRepository.searchByNameKeyword(keyword).map { it.toDomain() }
    }

    override fun deleteById(id: Long) {
        jpaOrganizationRepository.deleteById(id)
    }
}
