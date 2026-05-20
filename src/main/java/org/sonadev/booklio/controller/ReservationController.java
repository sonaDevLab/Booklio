package org.sonadev.booklio.controller;

import jakarta.validation.Valid;
import org.sonadev.booklio.dto.ReservationRequest;
import org.sonadev.booklio.dto.ReservationResponse;
import org.sonadev.booklio.dto.UpdateReservationRequest;
import org.sonadev.booklio.service.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    // Check AVAILABILITY
    @GetMapping("/availability")
    public boolean checkAvailability(
            @RequestParam Long resourceId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ){
        return reservationService.isAvailable(resourceId, startDate, endDate);
    }

    // Create reservation
    @PostMapping
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody ReservationRequest request) {
        return ResponseEntity.ok(reservationService.createReservation(request));
    }

    // Get reservation

    //by userId
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReservationResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(reservationService.getByUser(userId));
    }

    //by resourceId
    @GetMapping("/resource/{resourceId}")
    public ResponseEntity<List<ReservationResponse>> getByResource(@PathVariable Long resourceId) {
        return ResponseEntity.ok(reservationService.getByResource(resourceId));
    }

    //by date range
    @GetMapping("/by-date-range")
    public ResponseEntity<List<ReservationResponse>> getByDateRange(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(reservationService.getByDateRange(startDate, endDate));
    }

    // Cancel reservation
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ReservationResponse> cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }

    // Modify reservation
    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable Long id,
            @RequestBody UpdateReservationRequest request
    ){
        return ResponseEntity.ok(
                reservationService.updateReservation(id, request)
        );
    }

}
