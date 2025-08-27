package com.yourssu.spacer.spacehub.business.domain.organization

import com.yourssu.spacer.spacehub.implement.domain.authentication.PrivateClaims
import com.yourssu.spacer.spacehub.business.domain.authentication.TokenDto
import com.yourssu.spacer.spacehub.implement.domain.file.FileProcessor
import com.yourssu.spacer.spacehub.implement.support.security.password.PasswordFormat
import com.yourssu.spacer.spacehub.implement.support.security.password.PasswordValidator
import com.yourssu.spacer.spacehub.business.support.exception.DuplicateEmailException
import com.yourssu.spacer.spacehub.business.support.exception.UnauthorizedOrganizationException
import com.yourssu.spacer.spacehub.business.support.security.password.PasswordEncoder
import com.yourssu.spacer.spacehub.business.support.security.token.TokenEncoder
import com.yourssu.spacer.spacehub.implement.domain.authentication.TokenType
import com.yourssu.spacer.spacehub.implement.domain.organization.Organization
import com.yourssu.spacer.spacehub.implement.domain.organization.OrganizationName
import com.yourssu.spacer.spacehub.implement.domain.organization.OrganizationReader
import com.yourssu.spacer.spacehub.implement.domain.organization.OrganizationWriter
import java.time.LocalDateTime
import org.springframework.stereotype.Service

@Service
class OrganizationService(
    private val fileProcessor: FileProcessor,
    private val passwordEncoder: PasswordEncoder,
    private val organizationWriter: OrganizationWriter,
    private val organizationReader: OrganizationReader,
    private val tokenEncoder: TokenEncoder,
) {

    fun create(
        command: CreateOrganizationCommand,
    ): CreateOrganizationResult {
        PasswordValidator.validate(PasswordFormat.ORGANIZATION_PASSWORD, command.rawPassword)
        if (organizationReader.existByEmail(command.email)) {
            throw DuplicateEmailException("이미 존재하는 이메일입니다.")
        }
        val encryptedPassword: String = passwordEncoder.encode(command.rawPassword)
        val encryptedReservationPassword: String = passwordEncoder.encode(command.rawReservationPassword)

        val logoImageUrl: String = command.logoImage?.let {
            fileProcessor.upload(it)
        } ?: fileProcessor.getDefaultOrganizationImageUrl()

        val savedOrganization = organizationWriter.write(
            command,
            logoImageUrl,
            encryptedPassword,
            encryptedReservationPassword
        )

        // TODO: AuthenticationService.login() 메서드 코드와의 중복 해결
        val privateClaims = PrivateClaims(savedOrganization.id!!)
        val tokenDto: TokenDto = generateTokens(LocalDateTime.now(), privateClaims)

        return CreateOrganizationResult(savedOrganization.id, savedOrganization.getNameValue(), tokenDto)
    }

    private fun generateTokens(time: LocalDateTime, privateClaims: PrivateClaims): TokenDto {
        val accessToken: String = tokenEncoder.encode(time, TokenType.ACCESS, privateClaims.toMap())
        val refreshToken: String = tokenEncoder.encode(time, TokenType.REFRESH, privateClaims.toMap())

        return TokenDto(accessToken, refreshToken)
    }

    fun readById(organizationId: Long): OrganizationDto {
        val organization: Organization = organizationReader.getById(organizationId)

        return OrganizationDto.from(organization)
    }

    fun checkIsUnique(email: String): Boolean {
        return !organizationReader.existByEmail(email)
    }

    fun searchByNameKeyword(nameKeyword: String): ReadOrganizationsResult {
        val organizations: List<Organization> = organizationReader.searchByNameKeyword(nameKeyword)

        return ReadOrganizationsResult.from(organizations)
    }

    fun update(requestOrganizationId: Long, command: UpdateOrganizationCommand) {
        if (requestOrganizationId != command.targetOrganizationId) {
            throw UnauthorizedOrganizationException("본인의 단체 정보만 수정할 수 있습니다.")
        }
        val organization: Organization = organizationReader.getById(command.targetOrganizationId)

        val toUpdate: Organization = organization.updateAndReturnNew(
            name = OrganizationName(command.name),
            logoImageUrl = command.logoImage?.let { fileProcessor.upload(it) },
            description = command.description,
            encryptedReservationPassword = command.rawReservationPassword?.let { passwordEncoder.encode(it) },
        )

        organizationWriter.update(toUpdate, command.hashtags)
    }
}
