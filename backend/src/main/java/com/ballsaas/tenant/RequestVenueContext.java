package com.ballsaas.tenant;

import com.ballsaas.security.AuthContext;
import com.ballsaas.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class RequestVenueContext {

    private final AuthContext authContext;

    public RequestVenueContext(AuthContext authContext) {
        this.authContext = authContext;
    }

    public Long requireVenueId(HttpServletRequest request) {
        AuthenticatedUser user = authContext.currentUser(request).orElse(null);
        Long requestedVenueId = headerVenueId(request);
        if (user != null) {
            if (user.isPlatformAdmin()) {
                if (requestedVenueId == null) {
                    throw new VenueAccessDeniedException("平台视角访问场馆接口时必须指定场馆数据范围");
                }
                return requestedVenueId;
            }
            if (!user.isVenueRole() || user.venueId() == null) {
                throw new VenueAccessDeniedException("当前账号没有场馆数据权限");
            }
            if (requestedVenueId != null && !requestedVenueId.equals(user.venueId())) {
                throw new VenueAccessDeniedException("不能访问其他场馆数据");
            }
            return user.venueId();
        }
        if (requestedVenueId == null) {
            throw new VenueAccessDeniedException("缺少场馆数据范围");
        }
        return requestedVenueId;
    }

    public void assertVenue(Long expectedVenueId, HttpServletRequest request) {
        Long scopedVenueId = requireVenueId(request);
        if (!scopedVenueId.equals(expectedVenueId)) {
            throw new VenueAccessDeniedException("不能访问其他场馆数据");
        }
    }

    private Long headerVenueId(HttpServletRequest request) {
        String value = request.getHeader("X-Venue-Id");
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new VenueAccessDeniedException("场馆数据范围无效");
        }
    }
}