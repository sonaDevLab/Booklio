package org.sonadev.booklio.controller;

import jakarta.validation.Valid;
import org.sonadev.booklio.dto.CreateReservationRequest;
import org.sonadev.booklio.dto.ReservationResponse;
import org.sonadev.booklio.dto.UpdateReservationRequest;
import org.sonadev.booklio.service.ReservationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody CreateReservationRequest request) {
        return ResponseEntity.ok(reservationService.createReservation(request));
    }

    // Get reservations

    // All reservations
    @GetMapping()
    public ResponseEntity<Page<ReservationResponse>> getAllReservations(
            @PageableDefault(
                    size = 10,
                    sort = "startDate",
                    direction = Sort.Direction.ASC
            ) Pageable pageable
    ){
        return ResponseEntity.ok(reservationService.getAllReservations(pageable));
    }

    //by reservationId
    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> getReservationById(@PathVariable Long reservationId){
        return ResponseEntity.ok(reservationService.getReservationById(reservationId));
    }

    //by userId
    @GetMapping("/me")
    public ResponseEntity<List<ReservationResponse>> getMyReservations() {
        return ResponseEntity.ok(reservationService.getMyReservations());
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
            @Valid @RequestBody UpdateReservationRequest request
    ){
        return ResponseEntity.ok(
                reservationService.updateReservation(id, request)
        );
    }

}
