package org.sonadev.booklio.repository;

import org.sonadev.booklio.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
        SELECT r FROM Reservation r
        WHERE r.resource.id = :resourceId
        AND r.startDate < :endDate
        AND r.endDate > :startDate
    """)
    List<Reservation> findConflicts(Long resourceId, LocalDate startDate, LocalDate endDate);

}
