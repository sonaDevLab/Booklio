package org.sonadev.booklio.controller;

import jakarta.validation.Valid;
import org.sonadev.booklio.dto.ReservationRequest;
import org.sonadev.booklio.dto.ReservationResponse;
import org.sonadev.booklio.service.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody ReservationRequest request) {
        return ResponseEntity.ok(reservationService.createReservation(request));
    }

    // Check AVAILABILITY
    @GetMapping
    public boolean checkAvailability(
            @RequestParam Long resourceId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ){
        return reservationService.isAvailable(resourceId, startDate, endDate);
    }

    // Cancel reservation
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ReservationResponse> cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }

}
