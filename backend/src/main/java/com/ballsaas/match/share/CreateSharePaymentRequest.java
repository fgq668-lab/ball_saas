package com.ballsaas.match.share;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateSharePaymentRequest(
        @NotNull Long matchRoomId,
        Long userId,
        @Min(1) int amountCent
) {
}
