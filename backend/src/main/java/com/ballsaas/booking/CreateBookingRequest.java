package com.ballsaas.booking;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record CreateBookingRequest(
        @NotNull Long venueId,
        @NotNull Long courtId,
        Long userId,
        @NotNull @Future OffsetDateTime startAt,
        @NotNull @Future OffsetDateTime endAt
) {
}


