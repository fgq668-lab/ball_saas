package com.ballsaas.payment;

import com.ballsaas.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "payment_order")
public class PaymentOrder extends BaseEntity {

    @Column(nullable = false, unique = true, length = 40)
    private String paymentNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private BusinessType businessType;

    @Column(nullable = false)
    private Long businessId;

    @Column(nullable = false)
    private Long venueId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private int amountCent;

    @Column(nullable = false, length = 32)
    private String channel = "WECHAT";

    @Column(length = 120)
    private String wxPrepayId;

    @Column(length = 120)
    private String wxTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentStatus status = PaymentStatus.PENDING_PAYMENT;

    private OffsetDateTime paidAt;

    protected PaymentOrder() {
    }

    public PaymentOrder(String paymentNo, BusinessType businessType, Long businessId, Long venueId, Long userId, int amountCent) {
        this.paymentNo = paymentNo;
        this.businessType = businessType;
        this.businessId = businessId;
        this.venueId = venueId;
        this.userId = userId;
        this.amountCent = amountCent;
    }

    public String getPaymentNo() {
        return paymentNo;
    }

    public BusinessType getBusinessType() {
        return businessType;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public Long getVenueId() {
        return venueId;
    }

    public Long getUserId() {
        return userId;
    }

    public int getAmountCent() {
        return amountCent;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void markPaid(String wxTransactionId, OffsetDateTime paidAt) {
        if (this.status == PaymentStatus.PAID) {
            return;
        }
        this.wxTransactionId = wxTransactionId;
        this.paidAt = paidAt;
        this.status = PaymentStatus.PAID;
        touch();
    }

    public void markRefunded() {
        this.status = PaymentStatus.REFUNDED;
        touch();
    }

    public void markPartiallyRefunded() {
        this.status = PaymentStatus.PARTIALLY_REFUNDED;
        touch();
    }
}