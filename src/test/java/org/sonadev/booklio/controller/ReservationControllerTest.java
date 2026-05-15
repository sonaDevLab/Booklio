package org.sonadev.booklio.controller;

import org.junit.jupiter.api.Test;
import org.sonadev.booklio.dto.ReservationResponse;
import org.sonadev.booklio.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @MockitoBean
    private ReservationService reservationService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateReservation() throws Exception {

        // ARRANGE
        ReservationResponse response = new ReservationResponse();
        response.setId(1L);
        response.setUserId(1L);
        response.setResourceId(1L);
        response.setStartDate(LocalDate.of(2026, 5, 10));
        response.setEndDate(LocalDate.of(2026, 5, 12));

        when(reservationService.createReservation(any()))
                .thenReturn(response);

        // ACT + ASSERT (HTTP request)
        mockMvc.perform(post("/reservations")
                .contentType("application/json")
                .content("""
                {
                    "userId": 1,
                    "resourceId": 1,
                    "startDate": "2026-05-10",
                    "endDate": "2026-05-12"
                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.resourceId").value(1));

    }

    @Test
    void shoulReturnBadRequestWhenServiceFails() throws Exception {

        when(reservationService.createReservation(any()))
                .thenThrow(new RuntimeException("Resource not available"));

        mockMvc.perform(post("/reservations")
                .contentType("application/json")
                .content("""
                {
                    "userId": 1,
                    "resourceId": 1,
                    "startDate": "2026-05-10",
                    "endDate": "2026-05-12"
                }
                """))
                .andExpect(status().isBadRequest());
    }
}
