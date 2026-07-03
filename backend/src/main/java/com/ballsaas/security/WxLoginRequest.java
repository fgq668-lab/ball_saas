package com.ballsaas.security;

import jakarta.validation.constraints.NotBlank;

public record WxLoginRequest(@NotBlank String code) {
}

