package org.sonadev.booklio.integration;

import org.junit.jupiter.api.Test;
import org.sonadev.booklio.model.Reservation;
import org.sonadev.booklio.model.Resource;
import org.sonadev.booklio.model.User;
import org.sonadev.booklio.repository.ReservationRepository;
import org.sonadev.booklio.repository.ResourceRepository;
import org.sonadev.booklio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

@SpringBootTest
@Transactional
class ReservationRepositoryIntegrationTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Test
    void shouldSaveReservationInDatabase() {
        Reservation reservation = new Reservation();

        reservation.setStartDate(LocalDate.now());
        reservation.setEndDate(LocalDate.now().plusDays(2));

        Reservation saved = reservationRepository.save(reservation);

        assertNotNull(saved.getId());
    }

    @Test
    void shouldSaveReservationWithRelations() {
        User  user = new User();
        user.setName("Sona");
        user.setEmail("sona@test.com");

        user = userRepository.save(user);

        Resource resource = new Resource();
        resource.setName("Room A");

        resource = resourceRepository.save(resource);

        Reservation reservation = new Reservation();

        reservation.setUser(user);
        reservation.setResource(resource);

        reservation.setStartDate(LocalDate.now());
        reservation.setEndDate(LocalDate.now().plusDays(2));

        Reservation saved = reservationRepository.save(reservation);

        assertNotNull(saved.getId());

        assertEquals("Sona", saved.getUser().getName());
    }

}
