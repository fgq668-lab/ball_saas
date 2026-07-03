package com.ballsaas.security;

public record AuthenticatedUser(Long userId, Long venueId, String role) {

    public boolean isPlatformAdmin() {
        return "PLATFORM_ADMIN".equals(role);
    }

    public boolean isVenueRole() {
        return "VENUE_ADMIN".equals(role) || "VENUE_STAFF".equals(role);
    }
}
