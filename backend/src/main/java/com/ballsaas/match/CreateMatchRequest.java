package com.ballsaas.match;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateMatchRequest(
        @NotNull Long bookingOrderId,
        Long creatorUserId,
        @Min(1) int minPlayers,
        @Min(2) int maxPlayers
) {
}


