package com.ballsaas.match.share;

import com.ballsaas.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "match_share_payment", uniqueConstraints = {
        @UniqueConstraint(name = "uk_match_share_payment_user", columnNames = {"match_room_id", "user_id"})
})
public class MatchSharePayment extends BaseEntity {

    @Column(name = "match_room_id", nullable = false)
    private Long matchRoomId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    private Long paymentOrderId;

    @Transient
    private String paymentNo;

    @Column(nullable = false)
    private int amountCent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MatchSharePaymentStatus status = MatchSharePaymentStatus.PENDING;

    protected MatchSharePayment() {
    }

    public MatchSharePayment(Long matchRoomId, Long userId, int amountCent) {
        this.matchRoomId = matchRoomId;
        this.userId = userId;
        this.amountCent = amountCent;
    }

    public Long getMatchRoomId() {
        return matchRoomId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getPaymentOrderId() {
        return paymentOrderId;
    }

    public String getPaymentNo() {
        return paymentNo;
    }

    public int getAmountCent() {
        return amountCent;
    }

    public MatchSharePaymentStatus getStatus() {
        return status;
    }

    public void attachPaymentOrder(Long paymentOrderId) {
        this.paymentOrderId = paymentOrderId;
        touch();
    }

    public void attachPaymentOrder(Long paymentOrderId, String paymentNo) {
        this.paymentOrderId = paymentOrderId;
        this.paymentNo = paymentNo;
        touch();
    }

    public void markPaid() {
        this.status = MatchSharePaymentStatus.PAID;
        touch();
    }

    public void markRefunded() {
        this.status = MatchSharePaymentStatus.REFUNDED;
        touch();
    }
}
