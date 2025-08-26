package com.yourssu.spacer.spacehub.business.domain.space

import java.time.LocalTime
import org.springframework.web.multipart.MultipartFile

data class CreateSpaceCommand(
    val organizationId: Long,
    val name: String,
    val location: String,
    val spaceImage: MultipartFile? = null,
    val openingTime: LocalTime,
    val closingTime: LocalTime,
    val capacity: Int,
)
