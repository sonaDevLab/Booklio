package org.sonadev.booklio.service;

import org.sonadev.booklio.dto.ReservationRequest;
import org.sonadev.booklio.dto.ReservationResponse;
import org.sonadev.booklio.exception.InvalidReservationException;
import org.sonadev.booklio.exception.ReservationConflictException;
import org.sonadev.booklio.exception.ResourceNotFoundException;
import org.sonadev.booklio.model.Reservation;
import org.sonadev.booklio.model.Resource;
import org.sonadev.booklio.model.User;
import org.sonadev.booklio.repository.ReservationRepository;
import org.sonadev.booklio.repository.ResourceRepository;
import org.sonadev.booklio.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

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

    //Crear reserva
    public ReservationResponse createReservation(ReservationRequest dto){

        // Validar fechas
        if(dto.getStartDate().isAfter(dto.getEndDate())){
            throw new InvalidReservationException("Start date cannot be after end date");
        }

        // Buscar user y resource
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Resource resource = resourceRepository.findById(dto.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));

        // Comprobar disponibilidad
        boolean available = isAvailable(
                dto.getResourceId(),
                dto.getStartDate(),
                dto.getEndDate()
        );

        if(!available){
            throw new ReservationConflictException("Resource already booked");
        }

        // Crear reserva
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setResource(resource);
        reservation.setStartDate(dto.getStartDate());
        reservation.setEndDate(dto.getEndDate());

        Reservation saved = reservationRepository.save(reservation);

        // Mapear a response
        ReservationResponse response = new ReservationResponse();
        response.setId(saved.getId());
        response.setUserId(user.getId());
        response.setResourceId(resource.getId());
        response.setStartDate(saved.getStartDate());
        response.setEndDate(saved.getEndDate());

        return response;
    }
}
