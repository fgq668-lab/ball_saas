package com.ballsaas.booking;

import com.ballsaas.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;

@Entity
@Table(name = "court_time_slot", uniqueConstraints = {
        @UniqueConstraint(name = "uk_court_time_slot", columnNames = {"venue_id", "court_id", "slot_start_at"})
})
public class CourtTimeSlot extends BaseEntity {

    @Column(name = "venue_id", nullable = false)
    private Long venueId;

    @Column(name = "court_id", nullable = false)
    private Long courtId;

    @Column(name = "slot_start_at", nullable = false)
    private OffsetDateTime slotStartAt;

    @Column(name = "slot_end_at", nullable = false)
    private OffsetDateTime slotEndAt;

    @Column(name = "booking_order_id", nullable = false)
    private Long bookingOrderId;

    protected CourtTimeSlot() {
    }

    public CourtTimeSlot(Long venueId, Long courtId, OffsetDateTime slotStartAt, OffsetDateTime slotEndAt, Long bookingOrderId) {
        this.venueId = venueId;
        this.courtId = courtId;
        this.slotStartAt = slotStartAt;
        this.slotEndAt = slotEndAt;
        this.bookingOrderId = bookingOrderId;
    }
}

