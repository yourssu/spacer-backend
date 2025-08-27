package com.yourssu.spacer.spacehub.implement.domain.reservation

import com.yourssu.spacer.spacehub.implement.support.exception.PasswordNotEncryptedException
import com.yourssu.spacer.spacehub.implement.domain.space.Space
import com.yourssu.spacer.spacehub.implement.support.security.password.EncryptPasswordValidator
import java.time.LocalDateTime

class Reservation(
    val id: Long? = null,
    val space: Space,
    val bookerName: String,
    val reservationTime: ReservationTime,
    val encryptedPersonalPassword: String,
) {

    init {
        if (EncryptPasswordValidator.isNotEncrypted(encryptedPersonalPassword)) {
            throw PasswordNotEncryptedException("예약 취소에 사용할 비밀번호가 암호화되지 않았습니다.")
        }
    }

    fun getStartDateTime(): LocalDateTime {
        return reservationTime.startDateTime
    }

    fun getEndDateTime(): LocalDateTime {
        return reservationTime.endDateTime
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Reservation

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "Reservation(id=$id, space=$space, bookerName='$bookerName', reservationTime=$reservationTime)"
    }
}
