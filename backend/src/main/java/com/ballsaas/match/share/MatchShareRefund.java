package com.ballsaas.match.share;

import com.ballsaas.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "match_share_refund")
public class MatchShareRefund extends BaseEntity {

    @Column(nullable = false)
    private Long matchRoomId;

    @Column(nullable = false)
    private Long userId;

    private Long refundOrderId;

    @Column(nullable = false)
    private int amountCent;

    @Column(nullable = false, length = 32)
    private String status = "REQUESTED";

    protected MatchShareRefund() {
    }

    public MatchShareRefund(Long matchRoomId, Long userId, int amountCent) {
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

    public Long getRefundOrderId() {
        return refundOrderId;
    }

    public int getAmountCent() {
        return amountCent;
    }

    public String getStatus() {
        return status;
    }

    public void markRefunded() {
        this.status = "REFUNDED";
        touch();
    }
}
