package com.yourssu.spacer.spacehub.storage.domain.reservation

import com.yourssu.spacer.spacehub.implement.domain.reservation.Reservation
import com.yourssu.spacer.spacehub.implement.domain.reservation.ReservationTime
import com.yourssu.spacer.spacehub.storage.domain.space.SpaceEntity
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
import java.time.LocalDateTime

@Entity
@Table(name = "reservation")
class ReservationEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "space_id", nullable = false, foreignKey = ForeignKey(name = "fk_reservation_space"))
    val space: SpaceEntity,

    @Column(nullable = false)
    val bookerName: String,

    @Column(nullable = false)
    val startDateTime: LocalDateTime,

    @Column(nullable = false)
    val endDateTime: LocalDateTime,

    @Column(nullable = false)
    val encryptedPersonalPassword: String,
) {

    companion object {
        fun from(reservation: Reservation) = ReservationEntity(
            id = reservation.id,
            space = SpaceEntity.from(reservation.space),
            bookerName = reservation.bookerName,
            startDateTime = reservation.getStartDateTime(),
            endDateTime = reservation.getEndDateTime(),
            encryptedPersonalPassword = reservation.encryptedPersonalPassword,
        )
    }

    fun toDomain() = Reservation(
        id = id,
        space = space.toDomain(),
        bookerName = bookerName,
        reservationTime = ReservationTime(startDateTime, endDateTime),
        encryptedPersonalPassword = encryptedPersonalPassword,
    )
}
