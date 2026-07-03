package com.ballsaas.security;

import com.ballsaas.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthContext authContext;

    public AuthController(AuthContext authContext) {
        this.authContext = authContext;
    }

    @PostMapping("/api/app/auth/wx-login")
    public ApiResponse<LoginResponse> wxLogin(@Valid @RequestBody WxLoginRequest request) {
        return ApiResponse.ok(new LoginResponse(authContext.issueMockToken("APP_USER", 1L, null), 1L, null, "APP_USER"));
    }

    @PostMapping("/api/admin/auth/login")
    public ApiResponse<LoginResponse> adminLogin(@Valid @RequestBody AdminLoginRequest request) {
        return ApiResponse.ok(new LoginResponse(authContext.issueMockToken("PLATFORM_ADMIN", 1L, null), 1L, null, "PLATFORM_ADMIN"));
    }

    @PostMapping("/api/venue/auth/login")
    public ApiResponse<LoginResponse> venueLogin(@Valid @RequestBody AdminLoginRequest request) {
        boolean staff = "staff".equalsIgnoreCase(request.username());
        String role = staff ? "VENUE_STAFF" : "VENUE_ADMIN";
        Long userId = staff ? 3L : 2L;
        return ApiResponse.ok(new LoginResponse(authContext.issueMockToken(role, userId, 1L), userId, 1L, role));
    }
}
