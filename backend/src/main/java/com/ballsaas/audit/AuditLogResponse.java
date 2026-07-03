package com.ballsaas.audit;

import java.time.OffsetDateTime;

public record AuditLogResponse(
        Long id,
        String action,
        String objectType,
        Long objectId,
        Long operatorUserId,
        String operatorRole,
        Long venueId,
        String summary,
        OffsetDateTime createdAt) {

    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getAction(),
                log.getObjectType(),
                log.getObjectId(),
                log.getOperatorUserId(),
                log.getOperatorRole(),
                log.getVenueId(),
                log.getSummary(),
                log.getCreatedAt());
    }
}
