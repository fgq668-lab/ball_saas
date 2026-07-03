package com.ballsaas.payment;

import jakarta.validation.constraints.NotNull;

public record PrepayRequest(@NotNull Long bookingId) {
}

