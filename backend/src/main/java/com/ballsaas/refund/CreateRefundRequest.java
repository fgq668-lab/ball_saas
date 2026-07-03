package com.ballsaas.refund;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRefundRequest(
        @NotNull Long paymentOrderId,
        @NotNull Long venueId,
        @Min(1) int amountCent,
        @NotBlank String reason
) {
}

