package com.yourssu.spacer.spacehub.application.domain.organization

import com.yourssu.spacer.spacehub.application.support.authentication.AuthenticationOrganization
import com.yourssu.spacer.spacehub.application.support.authentication.AuthenticationOrganizationInfo
import com.yourssu.spacer.spacehub.business.domain.organization.CreateOrganizationResult
import com.yourssu.spacer.spacehub.business.domain.organization.OrganizationService
import com.yourssu.spacer.spacehub.business.domain.organization.ReadOrganizationsResult
import jakarta.validation.Valid
import java.net.URI
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class OrganizationController(
    private val organizationService: OrganizationService
) {

    @PostMapping("/organizations")
    fun create(
        @RequestPart(required = false) image: MultipartFile?,
        @RequestPart @Valid request: CreateOrganizationRequest,
    ): ResponseEntity<CreateOrganizationResponse> {
        System.out.println(request);
        val result: CreateOrganizationResult = organizationService.create(request.toCommand(image))
        val response = CreateOrganizationResponse.from(result)
        val organizationId: Long = result.id

        return ResponseEntity.created(URI.create("/organizations/$organizationId")).body(response)
    }

    @GetMapping("/check-email")
    fun checkEmail(
        @RequestParam email: String,
    ): ResponseEntity<CheckEmailResponse> {
        val isUnique = organizationService.checkIsUnique(email)
        val response = CheckEmailResponse(isUnique)

        return ResponseEntity.status(HttpStatus.OK).body(response)
    }

    @GetMapping("/organizations/{organizationId}")
    fun readById(
        @PathVariable organizationId: Long,
    ): ResponseEntity<ReadOrganizationResponse> {
        val organizationDto = organizationService.readById(organizationId)
        val response = ReadOrganizationResponse.from(organizationDto)

        return ResponseEntity.status(HttpStatus.OK).body(response)
    }

    @GetMapping("/organizations")
    fun readByName(
        @RequestParam name: String,
    ): ResponseEntity<List<ReadOrganizationResponse>> {
        val result: ReadOrganizationsResult = organizationService.searchByNameKeyword(name)
        val response: List<ReadOrganizationResponse> = result.organizationDtos.map {
            ReadOrganizationResponse.from(it)
        }

        return ResponseEntity.status(HttpStatus.OK).body(response)
    }

    @PatchMapping("/organizations/{organizationId}")
    fun update(
        @AuthenticationOrganization authInfo: AuthenticationOrganizationInfo,
        @PathVariable organizationId: Long,
        @RequestPart(required = false) image: MultipartFile?,
        @RequestPart @Valid request: UpdateOrganizationRequest,
    ): ResponseEntity<Unit> {
        organizationService.update(
            authInfo.organizationId,
            request.toCommand(organizationId, image)
        )

        return ResponseEntity.ok().build()
    }
}
