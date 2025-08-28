package com.yourssu.spacer.spacehub.implement.domain.organization

interface OrganizationRepository {

    fun save(organization: Organization): Organization
    fun existsById(id: Long): Boolean
    fun existsByEmail(email: String): Boolean
    fun findById(id: Long): Organization?
    fun findByEmail(email: String): Organization?
    fun searchByNameKeyword(keyword: String): List<Organization>

    fun deleteById(id: Long)
}
