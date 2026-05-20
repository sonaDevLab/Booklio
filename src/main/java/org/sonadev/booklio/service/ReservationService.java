package org.sonadev.booklio.service;

import org.sonadev.booklio.dto.ReservationRequest;
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
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;

    public ReservationService(ReservationRepository reservationRepository, UserRepository userRepository, ResourceRepository resourceRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
    }

    //Check if there's any available date for the reservation
    public boolean isAvailable(Long resourceId, LocalDate startDate, LocalDate endDate) {
        return reservationRepository.findConflicts(resourceId, startDate, endDate).isEmpty();
    }

    //Map to DTO
    private ReservationResponse mapToDTO(Reservation reservation) {
        ReservationResponse dto = new ReservationResponse();
        dto.setId(reservation.getId());
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
    public ReservationResponse createReservation(ReservationRequest dto){

        // Dates Validation
        if(dto.getStartDate().isAfter(dto.getEndDate())){
            throw new InvalidReservationException("Start date cannot be after end date");
        }

        // Search for user and resource
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

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

        // Map response
        ReservationResponse response = new ReservationResponse();
        response.setId(saved.getId());
        response.setUserId(user.getId());
        response.setResourceId(resource.getId());
        response.setStatus(ReservationStatus.CONFIRMED);
        response.setStartDate(saved.getStartDate());
        response.setEndDate(saved.getEndDate());

        return response;
    }

    //Get Reservation (userID)
    public List<ReservationResponse> getByUser(Long userId) {
        return reservationRepository.findByUserId(userId)
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
        Reservation reservation = reservationRepository
                .findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        // NO Double Cancellation
        if(reservation.getStatus() == ReservationStatus.CANCELLED){
            throw new InvalidReservationException("Reservation is already cancelled");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);

        reservationRepository.save(reservation);
    }

    //Update Reservation
    public ReservationResponse updateReservation(Long reservationId, UpdateReservationRequest request){
        Reservation reservation = reservationRepository
                .findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

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

        ReservationResponse response = new ReservationResponse();
        response.setId(updated.getId());
        response.setUserId(updated.getUser().getId());
        response.setResourceId(updated.getResource().getId());
        response.setStatus(updated.getStatus());
        response.setStartDate(updated.getStartDate());
        response.setEndDate(updated.getEndDate());

        return response;
    }
}
