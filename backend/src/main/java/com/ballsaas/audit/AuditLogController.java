package com.ballsaas.audit;

import com.ballsaas.common.ApiResponse;
import com.ballsaas.security.AuthContext;
import com.ballsaas.security.AuthenticatedUser;
import com.ballsaas.tenant.RequestVenueContext;
import com.ballsaas.tenant.VenueAccessDeniedException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;
    private final AuthContext authContext;
    private final RequestVenueContext venueContext;

    public AuditLogController(AuditLogRepository auditLogRepository, AuthContext authContext, RequestVenueContext venueContext) {
        this.auditLogRepository = auditLogRepository;
        this.authContext = authContext;
        this.venueContext = venueContext;
    }

    @GetMapping("/api/admin/audit-logs")
    public ApiResponse<List<AuditLogResponse>> listPlatformAuditLogs(HttpServletRequest request) {
        AuthenticatedUser user = authContext.requireUser(request);
        if (!user.isPlatformAdmin()) {
            throw new VenueAccessDeniedException("只有平台管理员可以查看平台审计日志");
        }
        return ApiResponse.ok(auditLogRepository.findTop100ByOrderByCreatedAtDesc().stream()
                .map(AuditLogResponse::from)
                .toList());
    }

    @GetMapping("/api/venue/audit-logs")
    public ApiResponse<List<AuditLogResponse>> listVenueAuditLogs(HttpServletRequest request) {
        Long scopedVenueId = venueContext.requireVenueId(request);
        return ApiResponse.ok(auditLogRepository.findTop100ByVenueIdOrderByCreatedAtDesc(scopedVenueId).stream()
                .map(AuditLogResponse::from)
                .toList());
    }
}
