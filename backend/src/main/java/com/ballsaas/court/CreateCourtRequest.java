package com.ballsaas.court;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCourtRequest(
        @NotNull Long venueId,
        @NotBlank String name,
        @NotBlank String sportType,
        boolean indoor
) {
}

