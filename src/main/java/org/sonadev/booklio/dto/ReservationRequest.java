package org.sonadev.booklio.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ReservationRequest {

    @NotNull(message = "UserId is required")
    private Long userId;

    @NotNull(message = "ResourceId is required")
    private Long resourceId;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

}
