package com.yourssu.spacer.spacehub.application.domain.organization

import com.yourssu.spacer.spacehub.business.domain.organization.CreateOrganizationCommand
import jakarta.validation.constraints.NotBlank
import org.springframework.web.multipart.MultipartFile

data class CreateOrganizationRequest(

    @NotBlank(message = "이메일이 입력되지 않았습니다.")
    val email: String,

    @NotBlank(message = "비밀번호가 입력되지 않았습니다.")
    val password: String,

    @NotBlank(message = "단체 이름이 입력되지 않았습니다.")
    val name: String,

    val description: String? = null,

    @NotBlank(message = "예약 비밀번호가 입력되지 않았습니다.")
    val reservationPassword: String,

    val hashtags: List<String> = emptyList(),
) {
    fun toCommand(logoImage: MultipartFile?): CreateOrganizationCommand {
        return CreateOrganizationCommand(
            email = email,
            rawPassword = password,
            name = name,
            logoImage = logoImage,
            description = description,
            rawReservationPassword = reservationPassword,
            hashtags = hashtags
        )
    }
}
