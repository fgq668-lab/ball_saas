package com.ballsaas.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ballsaas.security.AuthContext;
import com.ballsaas.tenant.RequestVenueContext;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

@ExtendWith(MockitoExtension.class)
class AuditLogControllerTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    private final AuthContext authContext = new AuthContext();

    @Test
    void platformAdminCanListRecentAuditLogs() {
        AuditLogController controller = new AuditLogController(auditLogRepository, authContext, new RequestVenueContext(authContext));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + authContext.issueMockToken("PLATFORM_ADMIN", 1L, null));
        when(auditLogRepository.findTop100ByOrderByCreatedAtDesc()).thenReturn(List.of());

        assertThat(controller.listPlatformAuditLogs(request).data()).isEmpty();

        verify(auditLogRepository).findTop100ByOrderByCreatedAtDesc();
    }

    @Test
    void venueAdminCanOnlyListScopedVenueAuditLogs() {
        AuditLogController controller = new AuditLogController(auditLogRepository, authContext, new RequestVenueContext(authContext));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + authContext.issueMockToken("VENUE_ADMIN", 2L, 9L));
        when(auditLogRepository.findTop100ByVenueIdOrderByCreatedAtDesc(9L)).thenReturn(List.of());

        assertThat(controller.listVenueAuditLogs(request).data()).isEmpty();

        verify(auditLogRepository).findTop100ByVenueIdOrderByCreatedAtDesc(9L);
    }
}
