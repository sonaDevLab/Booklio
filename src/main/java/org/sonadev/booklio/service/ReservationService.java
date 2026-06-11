package org.sonadev.booklio.service;

import lombok.AllArgsConstructor;
import org.sonadev.booklio.dto.CreateReservationRequest;
import org.sonadev.booklio.dto.ReservationResponse;
import org.sonadev.booklio.dto.UpdateReservationRequest;
import org.sonadev.booklio.exception.InvalidReservationException;
import org.sonadev.booklio.exception.ReservationConflictException;
import org.sonadev.booklio.exception.ResourceNotFoundException;
import org.sonadev.booklio.model.Reservation;
import org.sonadev.booklio.model.ReservationStatus;
import org.sonadev.booklio.model.Resource;
import org.sonadev.booklio.model.User;
import org.sonadev.booklio.repository.ReservationRepository;
import org.sonadev.booklio.repository.ResourceRepository;
import org.sonadev.booklio.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final SecurityService securityService;

    //Check if there's any available date for the reservation
    public boolean isAvailable(Long resourceId, LocalDate startDate, LocalDate endDate) {
        return reservationRepository.findConflicts(resourceId, startDate, endDate).isEmpty();
    }

    //Map to DTO
    private ReservationResponse mapToDTO(Reservation reservation) {
        ReservationResponse dto = new ReservationResponse();
        dto.setId(reservation.getId());
        dto.setStatus(reservation.getStatus());
        dto.setStartDate(reservation.getStartDate());
        dto.setEndDate(reservation.getEndDate());

        if(reservation.getUser() != null) {
            dto.setUserId(reservation.getUser().getId());
        }

        if(reservation.getResource() != null) {
            dto.setResourceId(reservation.getResource().getId());
        }

        return dto;
    }

    //Create Reservation
    public ReservationResponse createReservation(CreateReservationRequest dto){

        // Dates Validation
        if(dto.getStartDate().isAfter(dto.getEndDate())){
            throw new InvalidReservationException("Start date cannot be after end date");
        }

        // Search for user and resource
        User user = securityService.getAuthenticatedUser();

        Resource resource = resourceRepository.findById(dto.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));

        // Check availability
        boolean available = isAvailable(
                dto.getResourceId(),
                dto.getStartDate(),
                dto.getEndDate()
        );

        if(!available){
            throw new ReservationConflictException("Resource already booked");
        }

        // Create reservation
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setResource(resource);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setStartDate(dto.getStartDate());
        reservation.setEndDate(dto.getEndDate());

        Reservation saved = reservationRepository.save(reservation);

        return mapToDTO(saved);
    }

    //Get All Reservations
    public Page<ReservationResponse> getAllReservations(Pageable pageable) {
        return reservationRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    //Get Reservation (reservationId)
    public ReservationResponse getReservationById(Long reservationId){
        User user = securityService.getAuthenticatedUser();

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        if(!reservation.getUser().getId().equals(user.getId())){
            throw new AccessDeniedException("Not your reservation");
        }

        return mapToDTO(reservation);
    }

    //Get Reservation (userID)
    public List<ReservationResponse> getMyReservations() {
        User user = securityService.getAuthenticatedUser();

        return reservationRepository.findByUser(user)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    //Get Reservation (resourceID)
    public List<ReservationResponse> getByResource(Long resourceId) {
        return reservationRepository.findByResourceId(resourceId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    //Get Reservation (Date range)
    public List<ReservationResponse> getByDateRange(LocalDate startDate, LocalDate endDate) {
        return reservationRepository.findByDateRange(startDate, endDate)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    //Cancel Reservation
    public void cancelReservation(Long reservationId){
        User user = securityService.getAuthenticatedUser();

        Reservation reservation = reservationRepository
                .findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        if(!reservation.getUser().getId().equals(user.getId())){
            throw new AccessDeniedException("Not your reservation");
        }

        // NO Double Cancellation
        if(reservation.getStatus() == ReservationStatus.CANCELLED){
            throw new InvalidReservationException("Reservation is already cancelled");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);

        reservationRepository.save(reservation);
    }

    //Update Reservation
    public ReservationResponse updateReservation(Long reservationId, UpdateReservationRequest request){
        User user = securityService.getAuthenticatedUser();

        Reservation reservation = reservationRepository
                .findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        if(!reservation.getUser().getId().equals(user.getId())){
            throw new AccessDeniedException("Not your reservation");
        }

        if(reservation.getStatus() == ReservationStatus.CANCELLED){
            throw new InvalidReservationException("Cannot modify a cancelled reservation");
        }

        if(request.getStartDate().isAfter(request.getEndDate())){
            throw new InvalidReservationException("Start date cannot be after end date");
        }

        // Check conflicts
        List<Reservation> conflicts = reservationRepository.findConflicts(
                reservation.getResource().getId(),
                request.getStartDate(),
                request.getEndDate()
        ).stream()
            .filter(r -> !r.getId().equals(reservationId))
            .toList();

        if(!conflicts.isEmpty()){
            throw new ReservationConflictException("Resource not available for new dates");
        }

        // update
        reservation.setStartDate(request.getStartDate());
        reservation.setEndDate(request.getEndDate());

        Reservation updated = reservationRepository.save(reservation);

        return mapToDTO(updated);
    }
}
