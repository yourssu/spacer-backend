package com.yourssu.spacer.spacehub.storage.domain.organization

import com.yourssu.spacer.spacehub.implement.domain.organization.OrganizationHashtag
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "organization_hashtag")
class OrganizationHashtagEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val organizationId: Long,

    @Column(nullable = false)
    val hashtagId: Long,
) {

    companion object {
        fun from(organizationHashtag: OrganizationHashtag) = OrganizationHashtagEntity(
            organizationId = organizationHashtag.organizationId,
            hashtagId = organizationHashtag.hashtagId,
        )
    }

    fun toDomain() = OrganizationHashtag(
        id = id,
        organizationId = organizationId,
        hashtagId = hashtagId,
    )
}
