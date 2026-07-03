package com.ballsaas.security;

import com.ballsaas.tenant.VenueAccessDeniedException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class RequestUserContext {

    private final AuthContext authContext;

    public RequestUserContext(AuthContext authContext) {
        this.authContext = authContext;
    }

    public Optional<Long> currentUserId(HttpServletRequest request) {
        return authContext.currentUser(request).map(AuthenticatedUser::userId);
    }

    public Long requireCurrentUserId(HttpServletRequest request) {
        return currentUserId(request).orElseThrow(() -> new VenueAccessDeniedException("请先登录"));
    }

    public Long resolveUserId(HttpServletRequest request, Long requestedUserId) {
        Optional<Long> currentUserId = currentUserId(request);
        if (currentUserId.isPresent()) {
            Long userId = currentUserId.get();
            if (requestedUserId != null && !requestedUserId.equals(userId)) {
                throw new VenueAccessDeniedException("不能冒用其他用户身份");
            }
            return userId;
        }
        if (requestedUserId == null) {
            throw new VenueAccessDeniedException("缺少用户身份");
        }
        return requestedUserId;
    }
}
