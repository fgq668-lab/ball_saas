package com.ballsaas.match;

import com.ballsaas.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "match_room")
public class MatchRoom extends BaseEntity {

    @Column(nullable = false, unique = true, length = 40)
    private String matchNo;

    @Column(nullable = false)
    private Long bookingOrderId;

    @Column(nullable = false)
    private Long venueId;

    @Column(nullable = false)
    private Long courtId;

    @Column(nullable = false)
    private Long creatorUserId;

    @Column(nullable = false)
    private OffsetDateTime startAt;

    @Column(nullable = false)
    private OffsetDateTime endAt;

    @Column(nullable = false)
    private int minPlayers;

    @Column(nullable = false)
    private int maxPlayers;

    @Column(nullable = false)
    private int currentPlayers = 1;

    @Column(nullable = false, length = 32)
    private String payMode = "CREATOR_PAID";

    @Column(nullable = false)
    private int amountCent;

    @Column(nullable = false)
    private int perUserAmountCent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MatchStatus status = MatchStatus.RECRUITING;

    protected MatchRoom() {
    }

    public MatchRoom(String matchNo, Long bookingOrderId, Long venueId, Long courtId, Long creatorUserId, OffsetDateTime startAt, OffsetDateTime endAt, int minPlayers, int maxPlayers, int amountCent) {
        this.matchNo = matchNo;
        this.bookingOrderId = bookingOrderId;
        this.venueId = venueId;
        this.courtId = courtId;
        this.creatorUserId = creatorUserId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.amountCent = amountCent;
        this.perUserAmountCent = maxPlayers == 0 ? 0 : amountCent / maxPlayers;
    }

    public String getMatchNo() {
        return matchNo;
    }

    public Long getBookingOrderId() {
        return bookingOrderId;
    }

    public Long getVenueId() {
        return venueId;
    }

    public Long getCourtId() {
        return courtId;
    }

    public Long getCreatorUserId() {
        return creatorUserId;
    }

    public OffsetDateTime getStartAt() {
        return startAt;
    }

    public OffsetDateTime getEndAt() {
        return endAt;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getCurrentPlayers() {
        return currentPlayers;
    }

    public String getPayMode() {
        return payMode;
    }

    public int getAmountCent() {
        return amountCent;
    }

    public int getPerUserAmountCent() {
        return perUserAmountCent;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void playerJoined() {
        this.currentPlayers++;
        if (this.currentPlayers >= this.maxPlayers) {
            this.status = MatchStatus.FULL;
        }
        touch();
    }

    public void playerLeft() {
        if (this.currentPlayers > 1) {
            this.currentPlayers--;
        }
        if (this.status == MatchStatus.FULL && this.currentPlayers < this.maxPlayers) {
            this.status = MatchStatus.RECRUITING;
        }
        touch();
    }

    public void form() {
        this.status = MatchStatus.FORMED;
        touch();
    }

    public void cancelNotFormed() {
        this.status = MatchStatus.NOT_FORMED_CANCELLED;
        touch();
    }

    public boolean canChangePlayers() {
        return this.status == MatchStatus.RECRUITING || this.status == MatchStatus.FULL;
    }
}
