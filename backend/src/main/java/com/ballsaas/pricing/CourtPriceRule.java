package com.ballsaas.pricing;

import com.ballsaas.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "court_price_rule")
public class CourtPriceRule extends BaseEntity {

    @Column(nullable = false)
    private Long venueId;

    @Column(nullable = false)
    private Long courtId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DayType dayType;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private int priceCent;

    private LocalDate effectiveStartDate;

    private LocalDate effectiveEndDate;

    @Column(nullable = false)
    private int priority;

    @Column(nullable = false)
    private boolean enabled = true;

    protected CourtPriceRule() {
    }

    public CourtPriceRule(Long venueId, Long courtId, DayType dayType, LocalTime startTime, LocalTime endTime, int priceCent, int priority) {
        this.venueId = venueId;
        this.courtId = courtId;
        this.dayType = dayType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.priceCent = priceCent;
        this.priority = priority;
    }

    public int getPriceCent() {
        return priceCent;
    }
}

