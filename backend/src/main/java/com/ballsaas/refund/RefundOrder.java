package com.ballsaas.refund;

import com.ballsaas.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "refund_order")
public class RefundOrder extends BaseEntity {

    @Column(nullable = false, unique = true, length = 40)
    private String refundNo;

    @Column(nullable = false)
    private Long paymentOrderId;

    @Column(nullable = false)
    private Long venueId;

    @Column(nullable = false)
    private int amountCent;

    @Column(nullable = false, length = 200)
    private String reason;

    @Column(length = 120)
    private String wxRefundId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RefundStatus status = RefundStatus.REQUESTED;

    private OffsetDateTime requestedAt = OffsetDateTime.now();

    private OffsetDateTime refundedAt;

    protected RefundOrder() {
    }

    public RefundOrder(String refundNo, Long paymentOrderId, Long venueId, int amountCent, String reason) {
        this.refundNo = refundNo;
        this.paymentOrderId = paymentOrderId;
        this.venueId = venueId;
        this.amountCent = amountCent;
        this.reason = reason;
    }

    public String getRefundNo() {
        return refundNo;
    }

    public Long getPaymentOrderId() {
        return paymentOrderId;
    }

    public Long getVenueId() {
        return venueId;
    }

    public int getAmountCent() {
        return amountCent;
    }

    public String getReason() {
        return reason;
    }

    public RefundStatus getStatus() {
        return status;
    }

    public OffsetDateTime getRequestedAt() {
        return requestedAt;
    }

    public void markRefunded(String wxRefundId) {
        this.wxRefundId = wxRefundId;
        this.status = RefundStatus.REFUNDED;
        this.refundedAt = OffsetDateTime.now();
        touch();
    }
}