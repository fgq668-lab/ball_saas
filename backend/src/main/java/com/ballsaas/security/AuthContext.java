package com.ballsaas.security;

import com.ballsaas.tenant.VenueAccessDeniedException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class AuthContext {

    private static final String TOKEN_PREFIX = "mock:";

    public Optional<AuthenticatedUser> currentUser(HttpServletRequest request) {
        String value = request.getHeader("Authorization");
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String token = value.startsWith("Bearer ") ? value.substring(7) : value;
        if (!token.startsWith(TOKEN_PREFIX)) {
            throw new VenueAccessDeniedException("登录 token 无效");
        }
        String[] parts = token.split(":");
        if (parts.length < 4) {
            throw new VenueAccessDeniedException("登录 token 格式无效");
        }
        Long userId = parseLong(parts[2], "用户身份无效");
        Long venueId = "0".equals(parts[3]) ? null : parseLong(parts[3], "场馆身份无效");
        return Optional.of(new AuthenticatedUser(userId, venueId, parts[1]));
    }

    public AuthenticatedUser requireUser(HttpServletRequest request) {
        return currentUser(request).orElseThrow(() -> new VenueAccessDeniedException("请先登录"));
    }

    public String issueMockToken(String role, Long userId, Long venueId) {
        return TOKEN_PREFIX + role + ":" + userId + ":" + (venueId == null ? 0 : venueId) + ":" + System.currentTimeMillis();
    }

    private Long parseLong(String value, String message) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new VenueAccessDeniedException(message);
        }
    }
}
