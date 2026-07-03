package com.ballsaas.audit;

import com.ballsaas.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_log")
public class AuditLog extends BaseEntity {

    @Column(nullable = false, length = 80)
    private String action;

    @Column(nullable = false, length = 80)
    private String objectType;

    private Long objectId;

    private Long operatorUserId;

    @Column(length = 40)
    private String operatorRole;

    private Long venueId;

    @Column(length = 500)
    private String summary;

    protected AuditLog() {
    }

    public AuditLog(String action, String objectType, Long objectId, Long operatorUserId, String operatorRole, Long venueId, String summary) {
        this.action = action;
        this.objectType = objectType;
        this.objectId = objectId;
        this.operatorUserId = operatorUserId;
        this.operatorRole = operatorRole;
        this.venueId = venueId;
        this.summary = summary;
    }

    public String getAction() {
        return action;
    }

    public String getObjectType() {
        return objectType;
    }

    public Long getObjectId() {
        return objectId;
    }

    public Long getOperatorUserId() {
        return operatorUserId;
    }

    public String getOperatorRole() {
        return operatorRole;
    }

    public Long getVenueId() {
        return venueId;
    }

    public String getSummary() {
        return summary;
    }
}
