package com.ballsaas.booking;

import com.ballsaas.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "booking_order")
public class BookingOrder extends BaseEntity {

    @Column(nullable = false, unique = true, length = 40)
    private String orderNo;

    @Column(nullable = false)
    private Long venueId;

    @Column(nullable = false)
    private Long courtId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private OffsetDateTime startAt;

    @Column(nullable = false)
    private OffsetDateTime endAt;

    @Column(nullable = false)
    private int amountCent;

    @Column(nullable = false)
    private int payableCent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private BookingStatus status = BookingStatus.PENDING_PAYMENT;

    @Column(nullable = false, length = 80)
    private String lockToken;

    private OffsetDateTime paidAt;

    private OffsetDateTime cancelledAt;

    private OffsetDateTime checkedInAt;

    protected BookingOrder() {
    }

    public BookingOrder(String orderNo, Long venueId, Long courtId, Long userId, OffsetDateTime startAt, OffsetDateTime endAt, int amountCent, String lockToken) {
        this.orderNo = orderNo;
        this.venueId = venueId;
        this.courtId = courtId;
        this.userId = userId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.amountCent = amountCent;
        this.payableCent = amountCent;
        this.lockToken = lockToken;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public Long getVenueId() {
        return venueId;
    }

    public Long getCourtId() {
        return courtId;
    }

    public Long getUserId() {
        return userId;
    }

    public OffsetDateTime getStartAt() {
        return startAt;
    }

    public OffsetDateTime getEndAt() {
        return endAt;
    }

    public int getAmountCent() {
        return amountCent;
    }

    public int getPayableCent() {
        return payableCent;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void markReserved(OffsetDateTime paidAt) {
        this.status = BookingStatus.RESERVED;
        this.paidAt = paidAt;
        touch();
    }

    public void markPaymentTimeout() {
        this.status = BookingStatus.PAYMENT_TIMEOUT;
        this.cancelledAt = OffsetDateTime.now();
        touch();
    }

    public void cancel() {
        this.status = BookingStatus.CANCELLED;
        this.cancelledAt = OffsetDateTime.now();
        touch();
    }

    public void markManualReview() {
        this.status = BookingStatus.MANUAL_REVIEW;
        touch();
    }

    public void markRefunding() {
        this.status = BookingStatus.REFUNDING;
        touch();
    }

    public void markRefunded() {
        this.status = BookingStatus.REFUNDED;
        touch();
    }

    public void markPartiallyRefunded() {
        this.status = BookingStatus.PARTIALLY_REFUNDED;
        touch();
    }

    public void checkIn() {
        this.status = BookingStatus.CHECKED_IN;
        this.checkedInAt = OffsetDateTime.now();
        touch();
    }
}