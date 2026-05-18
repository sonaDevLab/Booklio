package org.sonadev.booklio.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sonadev.booklio.dto.ReservationRequest;
import org.sonadev.booklio.dto.ReservationResponse;
import org.sonadev.booklio.exception.InvalidReservationException;
import org.sonadev.booklio.exception.ReservationConflictException;
import org.sonadev.booklio.model.Reservation;
import org.sonadev.booklio.model.ReservationStatus;
import org.sonadev.booklio.model.Resource;
import org.sonadev.booklio.model.User;
import org.sonadev.booklio.repository.ReservationRepository;
import org.sonadev.booklio.repository.ResourceRepository;
import org.sonadev.booklio.repository.UserRepository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void shouldReturnTrueWhenNoConflictsExist(){
        when(reservationRepository.findConflicts(
                anyLong(),
                any(LocalDate.class),
                any(LocalDate.class)
        )).thenReturn(Collections.emptyList());

        boolean result = reservationService.isAvailable(
                1L,
                LocalDate.now(),
                LocalDate.now().plusDays(1)
        );

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenNoConflictsExist(){
        when(reservationRepository.findConflicts(
                anyLong(),
                any(LocalDate.class),
                any(LocalDate.class))
        ).thenReturn(List.of(new Reservation()));

        boolean result = reservationService.isAvailable(
                1L,
                LocalDate.now(),
                LocalDate.now().plusDays(2)
        );

        assertFalse(result);
    }

    @Test
    void shouldCreateReservationSuccessfully(){
        // ARRANGE
        ReservationRequest dto = new ReservationRequest();
        dto.setUserId(1L);
        dto.setResourceId(1L);
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(2));

        User user = new User();
        user.setId(1L);

        Resource resource = new Resource();
        resource.setId(1L);

        Reservation saved = new Reservation();
        saved.setId(100L);
        saved.setUser(user);
        saved.setResource(resource);
        saved.setStartDate(dto.getStartDate());
        saved.setEndDate(dto.getEndDate());

        // MOCK userRepository
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        // MOCK resourceRepository
        when(resourceRepository.findById(1L))
                .thenReturn(Optional.of(resource));

        // MOCK conflicts
        when(reservationRepository.findConflicts(
                anyLong(),
                any(LocalDate.class),
                any(LocalDate.class)
        )).thenReturn(Collections.emptyList());

        // MOCK save
        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(saved);

        // ACT
        ReservationResponse result = reservationService.createReservation(dto);

        // ASSERT
        assertNotNull(result);

        // VERIFY
        verify(reservationRepository, times(1))
                .save(any(Reservation.class));
    }

    @Test
    void shouldNotSaveReservationWhenConflictsExist(){
        ReservationRequest dto = new ReservationRequest();
        dto.setUserId(1L);
        dto.setResourceId(1L);
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(2));

        User user = new User();
        user.setId(1L);

        Resource resource = new Resource();
        resource.setId(1L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(resourceRepository.findById(1L))
                .thenReturn(Optional.of(resource));

        when(reservationRepository.findConflicts(
                anyLong(),
                any(LocalDate.class),
                any(LocalDate.class)
        )).thenReturn(List.of(new Reservation()));

        assertThrows(
                ReservationConflictException.class,
                () -> reservationService.createReservation(dto)
        );

        verify(reservationRepository, never())
                .save(any(Reservation.class));

    }

    @Test
    void shouldThrowExceptionWhenStartDateIsAfterEndDate(){
        ReservationRequest dto = new ReservationRequest();
        dto.setStartDate(LocalDate.now().plusDays(5));
        dto.setEndDate(LocalDate.now());

        InvalidReservationException exception = assertThrows(
                InvalidReservationException.class,
                () -> reservationService.createReservation(dto)
        );

        assertEquals("Start date cannot be after end date", exception.getMessage());
    }

    @Test
    void shouldCancelReservationSuccessfully(){
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        reservationService.cancelReservation(1L);

        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());

        verify(reservationRepository).save(reservation);
    }

}
