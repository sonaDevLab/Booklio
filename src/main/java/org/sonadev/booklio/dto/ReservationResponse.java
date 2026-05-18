package org.sonadev.booklio.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.sonadev.booklio.model.ReservationStatus;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {

    private Long id;
    private Long userId;
    private Long resourceId;
    private ReservationStatus status;
    private LocalDate startDate;
    private LocalDate endDate;

}
