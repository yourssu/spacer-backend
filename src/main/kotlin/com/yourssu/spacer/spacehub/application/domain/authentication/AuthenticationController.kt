package com.yourssu.spacer.spacehub.application.domain.authentication

import com.yourssu.spacer.spacehub.business.domain.authentication.AuthenticationService
import com.yourssu.spacer.spacehub.business.domain.authentication.LoginResultDto
import com.yourssu.spacer.spacehub.business.domain.authentication.TokenDto
import com.yourssu.spacer.spacehub.implement.domain.authentication.TokenType
import jakarta.validation.Valid
import java.time.LocalDateTime
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthenticationController(
    private val authenticationService: AuthenticationService,
) {

    @PostMapping("/login")
    fun login(
        @RequestBody @Valid request: LoginRequest,
    ): ResponseEntity<LoginResponse> {
        val loginResultDto: LoginResultDto = authenticationService.login(request.toCommand())
        val response = LoginResponse.from(loginResultDto)

        return ResponseEntity.ok(response)
    }

    @PostMapping("/logout")
    fun logout(
        @RequestHeader(HttpHeaders.AUTHORIZATION) accessToken: String,
        @RequestBody @Valid request: LogoutRequest,
    ): ResponseEntity<Unit> {
        authenticationService.logout(accessToken, request.refreshToken)

        return ResponseEntity.noContent().build()
    }

    @GetMapping("/token-validate")
    fun validateToken(
        @RequestHeader(HttpHeaders.AUTHORIZATION) accessToken: String,
    ): ResponseEntity<ValidateTokenResponse> {
        val validated: Boolean = authenticationService.isValidToken(TokenType.ACCESS, accessToken)
        val response = ValidateTokenResponse(validated)

        return ResponseEntity.ok(response)
    }

    @PostMapping("/token-refresh")
    fun refreshToken(
        @RequestBody @Valid request: TokenRefreshRequest,
    ): ResponseEntity<TokenRefreshResponse> {
        val requestTime = LocalDateTime.now()
        val tokenDto: TokenDto = authenticationService.refreshToken(requestTime, request.refreshToken)
        val response = TokenRefreshResponse.from(tokenDto)

        return ResponseEntity.ok(response)
    }

    @PostMapping("/withdrawal")
    fun withdraw(
        @RequestHeader(HttpHeaders.AUTHORIZATION) accessToken: String,
        @RequestBody @Valid request: WithdrawalRequest,
    ): ResponseEntity<Unit> {
        authenticationService.withdraw(accessToken, request.refreshToken)

        return ResponseEntity.noContent().build()
    }
}
