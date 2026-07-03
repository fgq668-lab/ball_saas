package com.ballsaas.match;

import com.ballsaas.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;

@Entity
@Table(name = "match_player", uniqueConstraints = {
        @UniqueConstraint(name = "uk_match_player_user", columnNames = {"match_room_id", "user_id"})
})
public class MatchPlayer extends BaseEntity {

    @Column(name = "match_room_id", nullable = false)
    private Long matchRoomId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MatchPlayerStatus status = MatchPlayerStatus.JOINED;

    private Long paymentOrderId;

    private OffsetDateTime joinedAt = OffsetDateTime.now();

    private OffsetDateTime leftAt;

    protected MatchPlayer() {
    }

    public MatchPlayer(Long matchRoomId, Long userId) {
        this.matchRoomId = matchRoomId;
        this.userId = userId;
    }

    public Long getMatchRoomId() {
        return matchRoomId;
    }

    public Long getUserId() {
        return userId;
    }

    public MatchPlayerStatus getStatus() {
        return status;
    }

    public Long getPaymentOrderId() {
        return paymentOrderId;
    }

    public OffsetDateTime getJoinedAt() {
        return joinedAt;
    }

    public OffsetDateTime getLeftAt() {
        return leftAt;
    }

    public void attachPaymentOrder(Long paymentOrderId) {
        this.paymentOrderId = paymentOrderId;
        touch();
    }

    public void markPaid() {
        this.status = MatchPlayerStatus.PAID;
        touch();
    }

    public void markLeft() {
        this.status = MatchPlayerStatus.LEFT;
        this.leftAt = OffsetDateTime.now();
        touch();
    }
}
