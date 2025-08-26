package com.yourssu.spacer.spacehub.storage.domain.space

import com.yourssu.spacer.spacehub.business.domain.space.Capacity
import com.yourssu.spacer.spacehub.business.domain.space.Space
import com.yourssu.spacer.spacehub.business.domain.space.SpaceOperatingTime
import com.yourssu.spacer.spacehub.storage.domain.organization.OrganizationEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalTime

@Entity
@Table(name = "space")
class SpaceEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organization_id", nullable = false, foreignKey = ForeignKey(name = "fk_space_organization"))
    val organization: OrganizationEntity,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val location: String,

    @Column
    val spaceImageUrl: String? = null,

    @Column(nullable = false)
    val openingTime: LocalTime,

    @Column(nullable = false)
    val closingTime: LocalTime,

    @Column(nullable = false)
    val capacity: Int,
) {

    companion object {
        fun from(space: Space) = SpaceEntity(
            id = space.id,
            organization = OrganizationEntity.from(space.organization),
            name = space.name,
            location = space.location,
            spaceImageUrl = space.spaceImageUrl,
            openingTime = space.getOpeningTime(),
            closingTime = space.getClosingTime(),
            capacity = space.getCapacityValue(),
        )
    }

    fun toDomain() = Space(
        id = id,
        organization = organization.toDomain(),
        name = name,
        location = location,
        spaceImageUrl = spaceImageUrl,
        operatingTime = SpaceOperatingTime(openingTime, closingTime),
        capacity = Capacity(capacity),
    )
}
