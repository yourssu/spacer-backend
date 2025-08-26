package com.yourssu.spacer.spacehub.application.support.exception

import com.yourssu.spacer.spacehub.application.support.authentication.LoginRequiredException
import com.yourssu.spacer.spacehub.application.support.authentication.NoSuchOrganizationException
import com.yourssu.spacer.spacehub.business.domain.authentication.EmptyTokenException
import com.yourssu.spacer.spacehub.business.domain.authentication.PasswordNotMatchException
import com.yourssu.spacer.spacehub.business.domain.file.InvalidFileException
import com.yourssu.spacer.spacehub.business.domain.file.ReadFailureException
import com.yourssu.spacer.spacehub.business.domain.file.StoreFailureException
import com.yourssu.spacer.spacehub.business.domain.file.UnsupportedFileExtensionException
import com.yourssu.spacer.spacehub.business.domain.organization.DuplicateEmailException
import com.yourssu.spacer.spacehub.business.domain.organization.InvalidEmailException
import com.yourssu.spacer.spacehub.business.domain.organization.InvalidOrganizationNameException
import com.yourssu.spacer.spacehub.business.domain.password.InvalidPasswordException
import com.yourssu.spacer.spacehub.business.domain.organization.OrganizationNotFoundException
import com.yourssu.spacer.spacehub.business.domain.password.PasswordNotEncryptedException
import com.yourssu.spacer.spacehub.business.domain.organization.UnauthorizedOrganizationException
import com.yourssu.spacer.spacehub.business.domain.reservation.InvalidReservationException
import com.yourssu.spacer.spacehub.business.domain.reservation.InvalidReservationTimeException
import com.yourssu.spacer.spacehub.business.domain.reservation.ReservationConflictException
import com.yourssu.spacer.spacehub.business.domain.reservation.ReservationNotFoundException
import com.yourssu.spacer.spacehub.business.domain.space.InvalidCapacityException
import com.yourssu.spacer.spacehub.business.domain.space.SpaceNotFoundException
import com.yourssu.spacer.spacehub.business.support.security.password.PasswordEncodingFailureException
import com.yourssu.spacer.spacehub.business.support.security.token.InvalidTokenException
import java.util.stream.Collectors
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(LoginRequiredException::class)
    fun handleLoginRequiredException(e: LoginRequiredException): ResponseEntity<UnauthorizedResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(UnauthorizedResponse(e.message, false))
    }

    @ExceptionHandler(NoSuchOrganizationException::class)
    fun handleNoSuchOrganizationException(e: NoSuchOrganizationException): ResponseEntity<UnauthorizedResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(UnauthorizedResponse(e.message, true))
    }

    @ExceptionHandler(EmptyTokenException::class)
    fun handleEmptyTokenException(e: EmptyTokenException): ResponseEntity<UnauthorizedResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(UnauthorizedResponse(e.message, false))
    }

    @ExceptionHandler(PasswordNotMatchException::class)
    fun handlePasswordNotMatchException(e: PasswordNotMatchException): ResponseEntity<UnauthorizedResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(UnauthorizedResponse(e.message, false))
    }

    @ExceptionHandler(InvalidFileException::class)
    fun handleInvalidFileException(e: InvalidFileException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ExceptionResponse(e.message))
    }

    @ExceptionHandler(UnsupportedFileExtensionException::class)
    fun handleUnsupportedFileExtensionException(e: UnsupportedFileExtensionException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ExceptionResponse(e.message))
    }

    @ExceptionHandler(StoreFailureException::class)
    fun handleStoreFailureException(e: StoreFailureException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ExceptionResponse(e.message))
    }

    @ExceptionHandler(ReadFailureException::class)
    fun handleReadFailureException(e: ReadFailureException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ExceptionResponse(e.message))
    }

    @ExceptionHandler(DuplicateEmailException::class)
    fun handleDuplicateEmailException(e: DuplicateEmailException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ExceptionResponse(e.message))
    }

    @ExceptionHandler(InvalidEmailException::class)
    fun handleInvalidEmailException(e: InvalidEmailException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ExceptionResponse(e.message))
    }

    @ExceptionHandler(InvalidOrganizationNameException::class)
    fun handleInvalidOrganizationNameException(e: InvalidOrganizationNameException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ExceptionResponse(e.message))
    }

    @ExceptionHandler(InvalidPasswordException::class)
    fun handleInvalidPasswordException(e: InvalidPasswordException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ExceptionResponse(e.message))
    }

    @ExceptionHandler(OrganizationNotFoundException::class)
    fun handleOrganizationNotFoundException(e: OrganizationNotFoundException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ExceptionResponse(e.message))
    }

    @ExceptionHandler(PasswordNotEncryptedException::class)
    fun handlePasswordNotEncryptedException(e: PasswordNotEncryptedException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ExceptionResponse(e.message))
    }

    @ExceptionHandler(UnauthorizedOrganizationException::class)
    fun handleUnauthorizedOrganizationException(e: UnauthorizedOrganizationException): ResponseEntity<UnauthorizedResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(UnauthorizedResponse(e.message, false))
    }

    @ExceptionHandler(InvalidReservationException::class)
    fun handleInvalidReservationException(e: InvalidReservationException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ExceptionResponse(e.message))
    }

    @ExceptionHandler(InvalidReservationTimeException::class)
    fun handleInvalidReservationTimeException(e: InvalidReservationTimeException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ExceptionResponse(e.message))
    }

    @ExceptionHandler(ReservationConflictException::class)
    fun handleReservationConflictException(e: ReservationConflictException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ExceptionResponse(e.message))
    }

    @ExceptionHandler(ReservationNotFoundException::class)
    fun handleReservationNotFoundException(e: ReservationNotFoundException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ExceptionResponse(e.message))
    }

    @ExceptionHandler(InvalidCapacityException::class)
    fun handleInvalidCapacityException(e: InvalidCapacityException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ExceptionResponse(e.message))
    }

    @ExceptionHandler(SpaceNotFoundException::class)
    fun handleSpaceNotFoundException(e: SpaceNotFoundException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ExceptionResponse(e.message))
    }

    @ExceptionHandler(PasswordEncodingFailureException::class)
    fun handlePasswordEncodingFailureException(e: PasswordEncodingFailureException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ExceptionResponse(e.message))
    }

    @ExceptionHandler(InvalidTokenException::class)
    fun handleInvalidTokenException(e: InvalidTokenException): ResponseEntity<UnauthorizedResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(UnauthorizedResponse(e.message, true))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        e: MethodArgumentNotValidException,
    ): ResponseEntity<ExceptionResponse> {
        val message = e.fieldErrors
            .stream()
            .map { obj: FieldError -> obj.defaultMessage }
            .collect(Collectors.joining("\n"))

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ExceptionResponse(message))
    }
}
