package com.yourssu.spacer.spacehub.application.domain.reservation

import com.yourssu.spacer.spacehub.business.domain.reservation.CreateReservationCommand
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class CreateReservationRequest(

    @NotBlank(message = "예약자 이름이 입력되지 않았습니다.")
    val name: String,

    @NotBlank(message = "예약 시작 시간이 입력되지 않았습니다.")
    @Future(message = "예약 시작 시간은 현재 시간 이후여야 합니다.")
    val startDateTime: LocalDateTime,

    @NotBlank(message = "예약 종료 시간이 입력되지 않았습니다.")
    @Future(message = "예약 종료 시간은 현재 시간 이후여야 합니다.")
    val endDateTime: LocalDateTime,

    @NotBlank(message = "예약 비밀번호가 입력되지 않았습니다.")
    val password: String,

    @NotBlank(message = "예약 취소에 사용할 비밀번호가 입력되지 않았습니다.")
    val personalPassword: String,
) {

    fun toCommand(
        spaceId: Long,
    ): CreateReservationCommand {
        return CreateReservationCommand(
            spaceId = spaceId,
            bookerName = name,
            startDateTime = startDateTime,
            endDateTime = endDateTime,
            password = password,
            rawPersonalPassword = personalPassword,
        )
    }
}
