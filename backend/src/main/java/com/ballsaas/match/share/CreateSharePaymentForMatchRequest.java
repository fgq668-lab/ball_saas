package com.ballsaas.match.share;

import jakarta.validation.constraints.Min;

public record CreateSharePaymentForMatchRequest(
        Long userId,
        @Min(1) int amountCent
) {
}
