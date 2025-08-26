package com.yourssu.spacer.spacehub.application.domain.space

import com.fasterxml.jackson.annotation.JsonFormat
import com.yourssu.spacer.spacehub.business.domain.space.UpdateSpaceOrganizationCommand
import jakarta.validation.constraints.NotBlank
import java.time.LocalTime
import org.springframework.web.multipart.MultipartFile

data class UpdateSpaceRequest(

    @NotBlank(message = "공간 이름이 입력되지 않았습니다.")
    val name: String,

    @NotBlank(message = "장소가 입력되지 않았습니다.")
    val location: String,

    @NotBlank(message = "오픈 시간이 입력되지 않았습니다.")
    @JsonFormat(pattern = "HH:mm")
    val openingTime: LocalTime,

    @NotBlank(message = "종료 시간이 입력되지 않았습니다.")
    @JsonFormat(pattern = "HH:mm")
    val closingTime: LocalTime,

    @NotBlank(message = "수용 인원이 입력되지 않았습니다.")
    val capacity: Int,
) {

    fun toCommand(
        targetSpaceId: Long, spaceImage: MultipartFile?
    ): UpdateSpaceOrganizationCommand = UpdateSpaceOrganizationCommand(
        targetSpaceId = targetSpaceId,
        name = name,
        location = location,
        spaceImage = spaceImage,
        openingTime = openingTime,
        closingTime = closingTime,
        capacity = capacity,
    )
}
