package com.ballsaas.audit;

import com.ballsaas.security.AuthContext;
import com.ballsaas.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final AuthContext authContext;

    public AuditService(AuditLogRepository auditLogRepository, AuthContext authContext) {
        this.auditLogRepository = auditLogRepository;
        this.authContext = authContext;
    }

    @Transactional
    public AuditLog record(String action, String objectType, Long objectId, Long venueId, HttpServletRequest request, String summary) {
        AuthenticatedUser user = authContext.currentUser(request).orElse(null);
        Long operatorUserId = user == null ? null : user.userId();
        String operatorRole = user == null ? "DEV_MOCK" : user.role();
        Long scopedVenueId = venueId != null ? venueId : (user == null ? null : user.venueId());
        return auditLogRepository.save(new AuditLog(action, objectType, objectId, operatorUserId, operatorRole, scopedVenueId, summary));
    }
}
