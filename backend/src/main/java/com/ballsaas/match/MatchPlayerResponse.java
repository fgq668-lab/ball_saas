package com.ballsaas.match;

import java.time.OffsetDateTime;

public record MatchPlayerResponse(
        Long id,
        Long matchRoomId,
        Long userId,
        String status,
        Long paymentOrderId,
        OffsetDateTime joinedAt,
        OffsetDateTime leftAt
) {

    static MatchPlayerResponse from(MatchPlayer player) {
        return new MatchPlayerResponse(
                player.getId(),
                player.getMatchRoomId(),
                player.getUserId(),
                player.getStatus().name(),
                player.getPaymentOrderId(),
                player.getJoinedAt(),
                player.getLeftAt()
        );
    }
}
