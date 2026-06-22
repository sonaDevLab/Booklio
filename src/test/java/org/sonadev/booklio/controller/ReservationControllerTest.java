package org.sonadev.booklio.controller;

import org.junit.jupiter.api.Test;
import org.sonadev.booklio.config.JwtService;
import org.sonadev.booklio.dto.ReservationResponse;
import org.sonadev.booklio.exception.GlobalExceptionHandler;
import org.sonadev.booklio.exception.InvalidReservationException;
import org.sonadev.booklio.exception.ReservationConflictException;
import org.sonadev.booklio.exception.ResourceNotFoundException;
import org.sonadev.booklio.service.CustomUserDetailsService;
import org.sonadev.booklio.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
@Import(GlobalExceptionHandler.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    /* CREATE */
    @Test
    @WithMockUser
    void shouldCreateReservation() throws Exception {

        // ARRANGE
        ReservationResponse response = new ReservationResponse();
        response.setId(1L);
        response.setUserId(1L);
        response.setResourceId(1L);
        response.setStartDate(LocalDate.of(2026, 8, 10));
        response.setEndDate(LocalDate.of(2026, 8, 12));

        when(reservationService.createReservation(any()))
                .thenReturn(response);

        // ACT + ASSERT (HTTP request)
        mockMvc.perform(post("/reservations")
                .contentType("application/json")
                .content("""
                {
                    "resourceId": 1,
                    "startDate": "2026-08-20",
                    "endDate": "2026-08-25"
                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.resourceId").value(1));

    }

    @Test
    void shouldReturnConflictWhenServiceFails() throws Exception {

        when(reservationService.createReservation(any()))
                .thenThrow(new ReservationConflictException("Resource not available"));

        mockMvc.perform(post("/reservations")
                .contentType("application/json")
                .content("""
                {
                    "userId": 1,
                    "resourceId": 1,
                    "startDate": "2026-08-20",
                    "endDate": "2026-08-25"
                }
                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status")
                        .value(409))
                .andExpect(jsonPath("$.error")
                        .value("Resource not available"));
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestWhenResourceIdIsNull() throws Exception {
        mockMvc.perform(post("/reservations")
                .contentType("application/json")
                .content("""
                {
                    "resourceId": null,
                    "startDate": "2026-08-20",
                    "endDate": "2026-08-25"
                }
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("ResourceId is required"));
    }

    /* GET */

    //get All
    @Test
    @WithMockUser
    void shouldGetPaginatedReservations() throws Exception {
        ReservationResponse response = new ReservationResponse();
        response.setId(1L);

        Page<ReservationResponse> page = new PageImpl<>(List.of(response));

        when(reservationService.getAllReservations(any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/reservations")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    @WithMockUser
    void shouldGetSortedReservations() throws Exception {
        ReservationResponse response = new ReservationResponse();
        response.setId(1L);

        Page<ReservationResponse> page = new PageImpl<>(List.of(response));

        when(reservationService.getAllReservations(any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/reservations")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "startDate,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    //by reservationId
    @Test
    @WithMockUser
    void shouldGetReservationById() throws Exception {
        ReservationResponse response = new ReservationResponse();
        response.setId(1L);

        when(reservationService.getReservationById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/reservations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser
    void shouldReturnNotFoundWhenReservationDoesNotExist() throws Exception {
        when(reservationService.getReservationById(1L))
                .thenThrow(new ResourceNotFoundException("Reservation not found"));

        mockMvc.perform(get("/reservations/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Reservation not found"));
    }

    //by user
    @Test
    @WithMockUser
    void shouldGetReservationByUser() throws Exception {
        ReservationResponse response = new ReservationResponse();
        response.setId(1L);
        response.setUserId(1L);

        when(reservationService.getMyReservations())
                .thenReturn(List.of(response));

        mockMvc.perform(get("/reservations/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].userId").value(1));
    }

    //by resource
    @Test
    @WithMockUser
    void shouldGetReservationByResource() throws Exception {
        ReservationResponse response = new ReservationResponse();
        response.setId(1L);
        response.setResourceId(1L);

        when(reservationService.getByResource(1L))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/reservations/resource/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resourceId").value(1));
    }

    //by date range
    @Test
    @WithMockUser
    void shouldGetReservationByDateRange() throws Exception {
        ReservationResponse response = new ReservationResponse();
        response.setId(1L);

        when(reservationService.getByDateRange(any(), any()))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/reservations/by-date-range")
                .param("startDate", "2026-08-01")
                .param("endDate", "2026-08-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    /* CANCEL */
    @Test
    @WithMockUser
    void shouldCancelReservation() throws Exception {
        doNothing().when(reservationService).cancelReservation(1L);

        mockMvc.perform(patch("/reservations/1/cancel"))
                .andExpect(status().isNoContent());

        verify(reservationService).cancelReservation(1L);
    }

    @Test
    @WithMockUser
    void shouldReturnNotFoundWhenCancellingNonExistingReservation() throws Exception {
        doThrow(new ResourceNotFoundException("Reservation not found"))
                .when(reservationService).cancelReservation(1L);

        mockMvc.perform(patch("/reservations/1/cancel"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Reservation not found"));
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestWhenReservationAlreadyCancelled() throws Exception {
        doThrow(new InvalidReservationException("Reservation is already cancelled"))
                .when(reservationService).cancelReservation(1L);

        mockMvc.perform(patch("/reservations/1/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Reservation is already cancelled"));
    }

    /* UPDATE  */
    @Test
    @WithMockUser
    void shouldUpdateReservation() throws Exception {
        ReservationResponse response = new ReservationResponse();
        response.setId(1L);
        response.setUserId(1L);
        response.setResourceId(1L);
        response.setStartDate(LocalDate.of(2026, 8, 1));
        response.setEndDate(LocalDate.of(2026, 8, 5));

        when(reservationService.updateReservation(anyLong(), any()))
                .thenReturn(response);

        mockMvc.perform(put("/reservations/1")
                .contentType("application/json")
                .content("""
                {
                    "startDate": "2026-08-01",
                    "endDate": "2026-08-05"
                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.startDate").value("2026-08-01"))
                .andExpect(jsonPath("$.endDate").value("2026-08-05"));
    }

    @Test
    @WithMockUser
    void shouldReturnConflictWhenUpdatingReservation() throws Exception {
        when(reservationService.updateReservation(anyLong(), any()))
                .thenThrow(new ReservationConflictException("Conflict"));

        mockMvc.perform(put("/reservations/1")
                .contentType("application/json")
                .content("""
                {
                    "startDate": "2026-08-01",
                    "endDate": "2026-08-05"
                }
                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @WithMockUser
    void shouldReturnNotFoundWhenUpdatingNonExistingReservation() throws Exception {
        when(reservationService.updateReservation(anyLong(), any()))
                .thenThrow(new ResourceNotFoundException("Reservation not found"));

        mockMvc.perform(put("/reservations/1")
                .contentType("application/json")
                .content("""
                {
                    "startDate": "2026-08-01",
                    "endDate": "2026-08-05"
                }
                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Reservation not found"));
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestWhenUpdatingCancelledReservation() throws Exception {
        when(reservationService.updateReservation(anyLong(), any()))
                .thenThrow(new InvalidReservationException("Cannot modify a cancelled reservation"));

        mockMvc.perform(put("/reservations/1")
                .contentType("application/json")
                .content("""
                {
                    "startDate": "2026-08-01",
                    "endDate": "2026-08-05"
                }
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Cannot modify a cancelled reservation"));
    }

}
