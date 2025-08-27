package com.yourssu.spacer.spacehub.business.domain.space

import com.yourssu.spacer.spacehub.implement.domain.file.FileProcessor
import com.yourssu.spacer.spacehub.implement.domain.organization.Organization
import com.yourssu.spacer.spacehub.implement.domain.organization.OrganizationReader
import com.yourssu.spacer.spacehub.business.support.exception.UnauthorizedOrganizationException
import com.yourssu.spacer.spacehub.implement.domain.space.Capacity
import com.yourssu.spacer.spacehub.implement.domain.space.Space
import com.yourssu.spacer.spacehub.implement.domain.space.SpaceOperatingTime
import com.yourssu.spacer.spacehub.implement.domain.space.SpaceReader
import com.yourssu.spacer.spacehub.implement.domain.space.SpaceWriter
import org.springframework.stereotype.Service

@Service
class SpaceService(
    private val fileProcessor: FileProcessor,
    private val organizationReader: OrganizationReader,
    private val spaceWriter: SpaceWriter,
    private val spaceReader: SpaceReader,
) {

    fun create(
        command: CreateSpaceCommand
    ): Long {
        val organization: Organization = organizationReader.getById(command.organizationId)
        val spaceImageUrl: String? = command.spaceImage?.let { fileProcessor.upload(it) }
        val savedSpace = spaceWriter.write(command, organization, spaceImageUrl)

        return savedSpace.id!!
    }

    fun readAllByOrganizationId(organizationId: Long): ReadSpacesResult {
        val organization: Organization = organizationReader.getById(organizationId)
        val spaces: List<Space> = spaceReader.readAllByOrganization(organization)

        return ReadSpacesResult.from(organization, spaces)
    }

    fun update(requestOrganizationId: Long, command: UpdateSpaceOrganizationCommand) {
        val organization: Organization = organizationReader.getById(requestOrganizationId)
        val space: Space = spaceReader.getById(command.targetSpaceId)
        if (organization != space.organization) {
            throw UnauthorizedOrganizationException("본인의 단체의 공간만 수정할 수 있습니다.")
        }

        val updatedSpace: Space = space.updateAndReturnNew(
            name = command.name,
            location = command.location,
            spaceImageUrl = command.spaceImage?.let { fileProcessor.upload(it) },
            operatingTime = SpaceOperatingTime(command.openingTime, command.closingTime),
            capacity = Capacity(command.capacity),
        )

        spaceWriter.update(updatedSpace)
    }

    fun readById(spaceId: Long): SpaceDto {
        val space: Space = spaceReader.getById(spaceId)

        return SpaceDto.from(space)
    }
}
