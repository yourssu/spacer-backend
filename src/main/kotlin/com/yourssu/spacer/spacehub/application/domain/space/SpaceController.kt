package com.yourssu.spacer.spacehub.application.domain.space

import com.yourssu.spacer.spacehub.application.support.authentication.AuthenticationOrganization
import com.yourssu.spacer.spacehub.application.support.authentication.AuthenticationOrganizationInfo
import com.yourssu.spacer.spacehub.business.domain.space.CreateSpaceCommand
import com.yourssu.spacer.spacehub.business.domain.space.ReadSpacesResult
import com.yourssu.spacer.spacehub.business.domain.space.SpaceService
import jakarta.validation.Valid
import java.net.URI
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
class SpaceController(
    private val spaceService: SpaceService,
) {

    @PostMapping("/spaces")
    fun create(
        @AuthenticationOrganization authInfo: AuthenticationOrganizationInfo,
        @RequestPart(required = false) image: MultipartFile?,
        @RequestPart @Valid request: CreateSpaceRequest,
    ): ResponseEntity<Unit> {
        val organizationId = authInfo.organizationId
        val command: CreateSpaceCommand = request.toCommand(organizationId, image)
        val spaceId = spaceService.create(command)

        return ResponseEntity.created(URI.create("/spaces/$spaceId")).build()
    }

    @GetMapping("/spaces/{spaceId}")
    fun readById(
        @PathVariable spaceId: Long,
    ): ResponseEntity<ReadSpaceResponse> {
        val space = spaceService.readById(spaceId)
        val response = ReadSpaceResponse.from(space)

        return ResponseEntity.ok(response)
    }

    @GetMapping("/spaces")
    fun readAllByOrganizationId(
        @RequestParam organizationId: Long,
    ): ResponseEntity<ReadSpacesResponse> {
        val spaces: ReadSpacesResult = spaceService.readAllByOrganizationId(organizationId)
        val response: ReadSpacesResponse = ReadSpacesResponse.from(spaces)

        return ResponseEntity.ok(response)
    }

    @PatchMapping("/spaces/{spaceId}")
    fun update(
        @AuthenticationOrganization authInfo: AuthenticationOrganizationInfo,
        @PathVariable spaceId: Long,
        @RequestPart(required = false) image: MultipartFile?,
        @RequestPart @Valid request: UpdateSpaceRequest,
    ): ResponseEntity<Unit> {
        spaceService.update(
            authInfo.organizationId,
            request.toCommand(spaceId, image)
        )

        return ResponseEntity.ok().build()
    }
}
