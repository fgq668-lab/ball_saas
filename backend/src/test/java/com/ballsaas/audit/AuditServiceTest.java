package com.ballsaas.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.ballsaas.security.AuthContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    private final AuthContext authContext = new AuthContext();

    @Test
    void recordsOperatorFromMockToken() {
        AuditService auditService = new AuditService(auditLogRepository, authContext);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + authContext.issueMockToken("VENUE_ADMIN", 2L, 9L));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuditLog log = auditService.record("BOOKING_CHECKIN", "BOOKING_ORDER", 88L, 9L, request, "核销预约订单");

        assertThat(log.getAction()).isEqualTo("BOOKING_CHECKIN");
        assertThat(log.getObjectType()).isEqualTo("BOOKING_ORDER");
        assertThat(log.getObjectId()).isEqualTo(88L);
        assertThat(log.getOperatorUserId()).isEqualTo(2L);
        assertThat(log.getOperatorRole()).isEqualTo("VENUE_ADMIN");
        assertThat(log.getVenueId()).isEqualTo(9L);
        assertThat(log.getSummary()).contains("核销");
    }

    @Test
    void recordsDevelopmentModeWhenNoToken() {
        AuditService auditService = new AuditService(auditLogRepository, authContext);
        MockHttpServletRequest request = new MockHttpServletRequest();
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuditLog log = auditService.record("VENUE_CREATE", "VENUE", 7L, 7L, request, "提交场馆入驻");

        assertThat(log.getOperatorUserId()).isNull();
        assertThat(log.getOperatorRole()).isEqualTo("DEV_MOCK");
        assertThat(log.getVenueId()).isEqualTo(7L);
    }
}
