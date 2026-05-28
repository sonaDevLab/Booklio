package org.sonadev.booklio.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonadev.booklio.model.Reservation;
import org.sonadev.booklio.model.ReservationStatus;
import org.sonadev.booklio.model.Resource;
import org.sonadev.booklio.model.User;
import org.sonadev.booklio.repository.ReservationRepository;
import org.sonadev.booklio.repository.ResourceRepository;
import org.sonadev.booklio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReservationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setup() {
        User user = new User();
        user.setName("Sona");
        user.setEmail("sona@test.com");
        user = userRepository.save(user);

        Resource resource = new Resource();
        resource.setName("Room A");
        resource.setType("Room");
        resource = resourceRepository.save(resource);

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setResource(resource);
        reservation.setStartDate(LocalDate.of(2026, 6, 15));
        reservation.setEndDate(LocalDate.of(2026, 6, 20));
        reservation.setStatus(ReservationStatus.CONFIRMED);

        reservationRepository.save(reservation);
    }

    /* CREATE */
    @Test
    void shouldCreateReservationInDatabase() throws Exception {
        mockMvc.perform(post("/reservations")
                .contentType("application/json")
                .content("""
                {
                    "userId": 1,
                    "resourceId": 1,
                    "startDate": "2026-06-01",
                    "endDate": "2026-06-05"
                }
                """))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.resourceId").value(1))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void shouldPersistReservationInDatabase() throws Exception {
        mockMvc.perform(post("/reservations")
                .contentType("application/json")
                .content("""
                {
                    "userId": 1,
                    "resourceId": 1,
                    "startDate": "2026-06-01",
                    "endDate": "2026-06-05"
                }
                """))
                .andExpect(status().isOk());

        List<Reservation> reservations = reservationRepository.findAll();

        assertEquals(1, reservations.size());
    }

    /* CANCEL */
    @Test
    void shouldCancelReservationAndUpdateStatusInDatabase() throws Exception {
        Reservation reservation = reservationRepository.findAll().get(0);

        Long id = reservation.getId();

        mockMvc.perform(patch("/reservations/" + id + "/cancel"))
                .andExpect(status().isNoContent());

        Reservation updated = reservationRepository.findById(id).orElseThrow();

        assertEquals(
                ReservationStatus.CANCELLED,
                updated.getStatus()
        );
    }

    /* UPDATE  */
    @Test
    void shouldUpdateReservationDatesInDatabase() throws Exception {
        Reservation reservation = reservationRepository.findAll().get(0);

        Long id = reservation.getId();

        mockMvc.perform(put("/reservations/" + id)
                .contentType("application/json")
                .content("""
                {
                    "startDate": "2026-07-01",
                    "endDate": "2026-07-10"
                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.startDate").value("2026-07-01"))
                .andExpect(jsonPath("$.endDate").value("2026-07-10"));

        Reservation updated = reservationRepository.findById(id).orElseThrow();

        assertEquals(
                LocalDate.of(2026, 7, 1),
                updated.getStartDate()
        );

        assertEquals(
                LocalDate.of(2026, 7, 10),
                updated.getEndDate()
        );
    }

    /* CONFLICTS  */
    @Test
    void ShouldReturnConflictWhenUpdatingWithOverlappingDates() throws Exception {
        User user = userRepository.findAll().get(0);

        Resource resource = resourceRepository.findAll().get(0);

        Reservation reservation2 = new Reservation();

        reservation2.setUser(user);
        reservation2.setResource(resource);

        reservation2.setStartDate(LocalDate.of(2026, 7, 5));
        reservation2.setEndDate(LocalDate.of(2026, 7, 15));

        reservation2.setStatus(ReservationStatus.CONFIRMED);

        reservationRepository.save(reservation2);

        Reservation reservation1 = reservationRepository.findAll().get(0);

        mockMvc.perform(put("/reservations/" + reservation1.getId())
                .contentType("application/json")
                .content("""
                {
                    "startDate": "2026-07-10",
                    "endDate": "2026-07-20"
                }
                """))
                .andExpect(status().isConflict());
    }

}
