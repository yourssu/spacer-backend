package com.yourssu.spacer.spacehub.implement.domain.space

import com.yourssu.spacer.spacehub.implement.domain.organization.Organization
import com.yourssu.spacer.spacehub.implement.domain.reservation.ReservationTime
import java.time.LocalTime

class Space(
    val id: Long? = null,
    val organization: Organization,
    val name: String,
    val location: String,
    val spaceImageUrl: String? = null,
    val operatingTime: SpaceOperatingTime,
    val capacity: Capacity,
) {

    fun canReserve(reservationTime: ReservationTime): Boolean {
        return operatingTime.isAvailableTime(reservationTime)
    }

    fun updateAndReturnNew(
        name: String? = this.name,
        location: String? = this.location,
        spaceImageUrl: String? = this.spaceImageUrl,
        operatingTime: SpaceOperatingTime? = this.operatingTime,
        capacity: Capacity? = this.capacity,
    ): Space {
        return Space(
            id = this.id,
            organization = organization,
            name = name ?: this.name,
            location = location ?: this.location,
            spaceImageUrl = spaceImageUrl ?: this.spaceImageUrl,
            operatingTime = operatingTime ?: this.operatingTime,
            capacity = capacity ?: this.capacity
        )
    }

    fun getEncryptedReservationPassword(): String {
        return organization.encryptedReservationPassword
    }

    fun getOpeningTime(): LocalTime {
        return operatingTime.openingTime
    }

    fun getClosingTime(): LocalTime {
        return operatingTime.closingTime
    }

    fun getCapacityValue(): Int {
        return capacity.value
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Space

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "Space(id=$id, organization=$organization, name='$name', location='$location', spaceImageUrl=$spaceImageUrl, operatingTime=$operatingTime, capacity=$capacity)"
    }
}
