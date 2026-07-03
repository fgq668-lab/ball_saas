package com.ballsaas.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.ballsaas.security.AuthContext;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

class RequestVenueContextTest {

    private final AuthContext authContext = new AuthContext();
    private final RequestVenueContext venueContext = new RequestVenueContext(authContext);
    private final HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);

    @Test
    void venueAdminUsesTokenVenueScope() {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + authContext.issueMockToken("VENUE_ADMIN", 2L, 9L));

        Long venueId = venueContext.requireVenueId(request);

        assertThat(venueId).isEqualTo(9L);
    }

    @Test
    void venueAdminCannotOverrideVenueScope() {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + authContext.issueMockToken("VENUE_ADMIN", 2L, 9L));
        when(request.getHeader("X-Venue-Id")).thenReturn("10");

        assertThatThrownBy(() -> venueContext.requireVenueId(request))
                .isInstanceOf(VenueAccessDeniedException.class)
                .hasMessageContaining("其他场馆");
    }

    @Test
    void platformAdminMustSpecifyVenueScope() {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + authContext.issueMockToken("PLATFORM_ADMIN", 1L, null));

        assertThatThrownBy(() -> venueContext.requireVenueId(request))
                .isInstanceOf(VenueAccessDeniedException.class)
                .hasMessageContaining("必须指定");
    }
}
