package com.yourssu.spacer.spacehub.application.domain.reservation

import com.yourssu.spacer.spacehub.business.domain.reservation.ReservationService
import jakarta.validation.Valid
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class ReservationController(
    private val reservationService: ReservationService,
) {

    @PostMapping("/spaces/{spaceId}/reservations")
    fun create(
        @PathVariable spaceId: Long,
        @RequestBody @Valid request: CreateReservationRequest,
    ): ResponseEntity<Unit> {
        val reservationId = reservationService.create(request.toCommand(spaceId))

        return ResponseEntity.created(URI.create("/spaces/$spaceId/reservations/$reservationId")).build()
    }

    @GetMapping("/spaces/{spaceId}/reservations")
    fun readAll(
        @PathVariable spaceId: Long,
        @RequestParam(required = false) date: LocalDate?,
        @RequestParam(required = false) time: LocalDateTime?
    ): ResponseEntity<List<ReadReservationResponse>> {
        val result = when {
            date != null -> reservationService.readAllByDate(spaceId, date)
            time != null -> reservationService.readAllAfterTime(spaceId, time)
            else -> throw IllegalArgumentException("Either date or time must be provided")
        }

        val responses: List<ReadReservationResponse> = result.reservationDtos.map { ReadReservationResponse.from(it) }

        return ResponseEntity.ok(responses)
    }

    @DeleteMapping("/reservations/{reservationId}")
    fun delete(
        @PathVariable reservationId: Long,
        @RequestBody @Valid request: DeleteReservationRequest,
    ): ResponseEntity<Unit> {
        reservationService.delete(reservationId, request.personalPassword)

        return ResponseEntity.noContent().build()
    }
}
