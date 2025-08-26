package com.yourssu.spacer.spacehub.business.domain.organization

import org.springframework.web.multipart.MultipartFile

data class UpdateOrganizationCommand(

    val targetOrganizationId: Long,
    val name: String,
    val logoImage: MultipartFile? = null,
    val description: String? = null,
    val rawReservationPassword: String? = null,
    val hashtags: List<String> = emptyList(),
)
