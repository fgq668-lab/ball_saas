package com.ballsaas.court;

import com.ballsaas.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "court")
public class Court extends BaseEntity {

    @Column(nullable = false)
    private Long venueId;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, length = 32)
    private String sportType;

    @Column(nullable = false)
    private boolean indoor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CourtStatus status = CourtStatus.ENABLED;

    @Column(nullable = false)
    private int sortOrder = 0;

    protected Court() {
    }

    public Court(Long venueId, String name, String sportType, boolean indoor) {
        this.venueId = venueId;
        this.name = name;
        this.sportType = sportType;
        this.indoor = indoor;
    }

    public Long getVenueId() {
        return venueId;
    }

    public String getName() {
        return name;
    }

    public String getSportType() {
        return sportType;
    }

    public CourtStatus getStatus() {
        return status;
    }

    public void disable() {
        this.status = CourtStatus.DISABLED;
        touch();
    }
}

