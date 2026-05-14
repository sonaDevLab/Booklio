package org.sonadev.booklio.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ReservationRequest {

    private Long userId;
    private Long resourceId;
    private LocalDate startDate;
    private LocalDate endDate;

}
