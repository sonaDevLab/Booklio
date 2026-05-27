package org.sonadev.booklio.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonadev.booklio.model.Reservation;
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
        userRepository.save(user);

        Resource resource = new Resource();
        resource.setName("Room A");
        resource.setType("Room");
        resourceRepository.save(resource);
    }

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

}
