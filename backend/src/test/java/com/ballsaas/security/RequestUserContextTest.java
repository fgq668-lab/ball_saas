package com.ballsaas.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ballsaas.tenant.VenueAccessDeniedException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class RequestUserContextTest {

    private final AuthContext authContext = new AuthContext();
    private final RequestUserContext userContext = new RequestUserContext(authContext);

    @Test
    void resolvesRequestedUserInDevelopmentModeWithoutToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        Long userId = userContext.resolveUserId(request, 88L);

        assertThat(userId).isEqualTo(88L);
    }

    @Test
    void resolvesTokenUserWhenRequestMatches() {
        MockHttpServletRequest request = requestWithToken(88L);

        Long userId = userContext.resolveUserId(request, 88L);

        assertThat(userId).isEqualTo(88L);
    }

    @Test
    void resolvesTokenUserWithoutRequestUserId() {
        MockHttpServletRequest request = requestWithToken(88L);

        Long userId = userContext.resolveUserId(request, null);

        assertThat(userId).isEqualTo(88L);
    }

    @Test
    void rejectsUserIdSpoofingWhenTokenIsPresent() {
        MockHttpServletRequest request = requestWithToken(88L);

        assertThatThrownBy(() -> userContext.resolveUserId(request, 99L))
                .isInstanceOf(VenueAccessDeniedException.class)
                .hasMessageContaining("不能冒用其他用户身份");
    }

    @Test
    void rejectsMissingUserInDevelopmentMode() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertThatThrownBy(() -> userContext.resolveUserId(request, null))
                .isInstanceOf(VenueAccessDeniedException.class)
                .hasMessageContaining("缺少用户身份");
    }

    private MockHttpServletRequest requestWithToken(Long userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + authContext.issueMockToken("APP_USER", userId, null));
        return request;
    }
}
