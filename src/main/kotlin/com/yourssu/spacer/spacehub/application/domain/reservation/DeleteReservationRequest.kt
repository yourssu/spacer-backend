package com.yourssu.spacer.spacehub.application.domain.reservation

import jakarta.validation.constraints.NotBlank

data class DeleteReservationRequest(

    @NotBlank(message = "비밀번호가 입력되지 않았습니다.")
    val personalPassword: String,
)
