package com.ballsaas.venue;

import com.ballsaas.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "venue")
public class Venue extends BaseEntity {

    private Long tenantId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 200)
    private String sportTypes;

    @Column(nullable = false, length = 300)
    private String address;

    private BigDecimal longitude;

    private BigDecimal latitude;

    @Column(nullable = false, length = 60)
    private String contactName;

    @Column(nullable = false, length = 30)
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private VenueStatus status = VenueStatus.PENDING_REVIEW;

    @Column(length = 64)
    private String wxSubMchId;

    protected Venue() {
    }

    public Venue(String name, String sportTypes, String address, String contactName, String contactPhone) {
        this.name = name;
        this.sportTypes = sportTypes;
        this.address = address;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getName() {
        return name;
    }

    public String getSportTypes() {
        return sportTypes;
    }

    public String getAddress() {
        return address;
    }

    public VenueStatus getStatus() {
        return status;
    }

    public String getWxSubMchId() {
        return wxSubMchId;
    }

    public void approve() {
        this.status = VenueStatus.APPROVED;
        touch();
    }

    public void reject() {
        this.status = VenueStatus.REJECTED;
        touch();
    }

    public void disable() {
        this.status = VenueStatus.DISABLED;
        touch();
    }
}

