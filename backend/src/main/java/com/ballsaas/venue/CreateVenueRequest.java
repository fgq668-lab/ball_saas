package com.ballsaas.venue;

import jakarta.validation.constraints.NotBlank;

public record CreateVenueRequest(
        @NotBlank String name,
        @NotBlank String sportTypes,
        @NotBlank String address,
        @NotBlank String contactName,
        @NotBlank String contactPhone
) {
}

