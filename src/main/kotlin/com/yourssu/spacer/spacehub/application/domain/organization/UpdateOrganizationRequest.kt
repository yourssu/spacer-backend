package com.yourssu.spacer.spacehub.application.domain.organization

import com.yourssu.spacer.spacehub.business.domain.organization.UpdateOrganizationCommand
import jakarta.validation.constraints.NotBlank
import org.springframework.web.multipart.MultipartFile

data class UpdateOrganizationRequest(

    @NotBlank(message = "단체 이름이 입력되지 않았습니다.")
    val name: String,
    val description: String? = null,
    val reservationPassword: String? = null,
    val hashtags: List<String> = emptyList(),
) {
    fun toCommand(
        targetOrganizationId: Long,
        logoImage: MultipartFile?
    ): UpdateOrganizationCommand = UpdateOrganizationCommand(
        targetOrganizationId = targetOrganizationId,
        name = name,
        logoImage = logoImage,
        description = description,
        rawReservationPassword = reservationPassword,
        hashtags = hashtags
    )
}
