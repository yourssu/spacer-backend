package com.yourssu.spacer.spacehub.storage.domain.organization

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface JpaOrganizationRepository : JpaRepository<OrganizationEntity, Long> {

    fun existsByEmail(email: String): Boolean
    fun findByEmail(email: String): OrganizationEntity?

    @Query("""
        SELECT o
        FROM OrganizationEntity o
        WHERE o.name LIKE %:keyword%
        order by o.id desc
    """)
    fun searchByNameKeyword(keyword: String): List<OrganizationEntity>
}
