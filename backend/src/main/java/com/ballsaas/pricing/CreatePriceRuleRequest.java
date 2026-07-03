package com.ballsaas.pricing;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record CreatePriceRuleRequest(
        @NotNull Long venueId,
        @NotNull Long courtId,
        @NotNull DayType dayType,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        @Min(1) int priceCent,
        int priority
) {
}

