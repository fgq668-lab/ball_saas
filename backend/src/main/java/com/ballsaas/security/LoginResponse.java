package com.ballsaas.security;

public record LoginResponse(String token, Long userId, Long venueId, String role) {
}

