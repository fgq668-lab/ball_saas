package com.ballsaas.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AuthControllerTest {

    private final AuthController authController = new AuthController(new AuthContext());

    @Test
    void venueLoginIssuesVenueAdminTokenByDefault() {
        LoginResponse response = authController.venueLogin(new AdminLoginRequest("venue", "venue")).data();

        assertThat(response.role()).isEqualTo("VENUE_ADMIN");
        assertThat(response.userId()).isEqualTo(2L);
        assertThat(response.venueId()).isEqualTo(1L);
        assertThat(response.token()).startsWith("mock:VENUE_ADMIN:2:1:");
    }

    @Test
    void venueLoginCanIssueVenueStaffTokenForDevelopmentTesting() {
        LoginResponse response = authController.venueLogin(new AdminLoginRequest("staff", "staff")).data();

        assertThat(response.role()).isEqualTo("VENUE_STAFF");
        assertThat(response.userId()).isEqualTo(3L);
        assertThat(response.venueId()).isEqualTo(1L);
        assertThat(response.token()).startsWith("mock:VENUE_STAFF:3:1:");
    }
}
