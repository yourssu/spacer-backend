package com.yourssu.spacer.spacehub.business.domain.authentication

import com.yourssu.spacer.spacehub.application.support.authentication.NoSuchOrganizationException
import com.yourssu.spacer.spacehub.business.domain.organization.Organization
import com.yourssu.spacer.spacehub.business.domain.organization.OrganizationReader
import com.yourssu.spacer.spacehub.business.domain.organization.OrganizationWriter
import com.yourssu.spacer.spacehub.business.support.security.password.PasswordEncoder
import com.yourssu.spacer.spacehub.business.support.security.token.InvalidTokenException
import com.yourssu.spacer.spacehub.business.support.security.token.TokenDecoder
import com.yourssu.spacer.spacehub.business.support.security.token.TokenEncoder
import com.yourssu.spacer.spacehub.business.support.security.token.TokenType
import io.jsonwebtoken.Claims
import java.time.LocalDateTime
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val organizationWriter: OrganizationWriter,
    private val organizationReader: OrganizationReader,
    private val blacklistTokenWriter: BlacklistTokenWriter,
    private val blacklistTokenReader: BlacklistTokenReader,
    private val passwordEncoder: PasswordEncoder,
    private val tokenEncoder: TokenEncoder,
    private val tokenDecoder: TokenDecoder,
) {

    fun login(loginCommand: LoginCommand): LoginResultDto {
        val organization: Organization = organizationReader.getByEmail(loginCommand.email)
        if (!passwordEncoder.matches(loginCommand.password, organization.encryptedPassword)) {
            throw PasswordNotMatchException("비밀번호가 일치하지 않습니다.")
        }

        val privateClaims = PrivateClaims(organization.id!!)
        val tokenDto: TokenDto = generateTokens(loginCommand.requestTime, privateClaims)

        return LoginResultDto(organization.id, organization.getNameValue(), tokenDto)
    }

    private fun generateTokens(time: LocalDateTime, privateClaims: PrivateClaims): TokenDto {
        val accessToken: String = tokenEncoder.encode(time, TokenType.ACCESS, privateClaims.toMap())
        val refreshToken: String = tokenEncoder.encode(time, TokenType.REFRESH, privateClaims.toMap())

        return TokenDto(accessToken, refreshToken)
    }

    fun logout(accessToken: String, refreshToken: String) {
        val organizationId = getValidOrganizationId(TokenType.ACCESS, accessToken)
        blacklistTokenWriter.register(organizationId, accessToken, refreshToken)
    }

    fun isValidToken(tokenType: TokenType, targetToken: String): Boolean {
        val claims: Claims = tokenDecoder.decode(tokenType, targetToken)
            ?: return false

        val organizationId = PrivateClaims.from(claims).organizationId

        return organizationReader.existsById(organizationId) &&
                !blacklistTokenReader.isBlacklisted(organizationId, targetToken)
    }

    fun refreshToken(requestTime: LocalDateTime, refreshToken: String): TokenDto {
        val privateClaims = decode(TokenType.REFRESH, refreshToken)

        return generateTokens(requestTime, privateClaims)
    }

    private fun decode(tokenType: TokenType, accessToken: String): PrivateClaims {
        val claims: Claims = tokenDecoder.decode(tokenType, accessToken)
            ?: throw InvalidTokenException("유효한 토큰이 아닙니다.")

        return PrivateClaims.from(claims)
    }

    fun getValidOrganizationId(tokenType: TokenType, token: String): Long {
        val privateClaims: PrivateClaims = decode(tokenType, token)
        val organizationId = privateClaims.organizationId
        if (!organizationReader.existsById(organizationId)) {
            throw NoSuchOrganizationException("존재하지 않는 단체의 토큰입니다.")
        }
        if (blacklistTokenReader.isBlacklisted(organizationId, token)) {
            throw InvalidTokenException("로그아웃되었습니다.")
        }

        return organizationId
    }

    fun withdraw(accessToken: String, refreshToken: String) {
        val organizationId = getValidOrganizationId(TokenType.ACCESS, accessToken)
        blacklistTokenWriter.register(organizationId, accessToken, refreshToken)
        organizationWriter.withdraw(organizationId)
    }
}
