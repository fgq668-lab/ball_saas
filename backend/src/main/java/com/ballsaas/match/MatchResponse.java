package com.ballsaas.match;

import java.time.OffsetDateTime;

public record MatchResponse(
        Long id,
        String matchNo,
        Long bookingOrderId,
        Long venueId,
        Long courtId,
        Long creatorUserId,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        int minPlayers,
        int maxPlayers,
        int currentPlayers,
        int amountCent,
        int perUserAmountCent,
        String payMode,
        String status
) {

    static MatchResponse from(MatchRoom room) {
        return new MatchResponse(
                room.getId(),
                room.getMatchNo(),
                room.getBookingOrderId(),
                room.getVenueId(),
                room.getCourtId(),
                room.getCreatorUserId(),
                room.getStartAt(),
                room.getEndAt(),
                room.getMinPlayers(),
                room.getMaxPlayers(),
                room.getCurrentPlayers(),
                room.getAmountCent(),
                room.getPerUserAmountCent(),
                room.getPayMode(),
                room.getStatus().name()
        );
    }
}