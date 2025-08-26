package com.yourssu.spacer.spacehub.business.domain.organization

import org.springframework.web.multipart.MultipartFile

data class CreateOrganizationCommand(
    val email: String,
    val rawPassword: String,
    val name: String,
    val logoImage: MultipartFile? = null,
    val description: String? = null,
    val rawReservationPassword: String,
    val hashtags: List<String> = emptyList(),
)
