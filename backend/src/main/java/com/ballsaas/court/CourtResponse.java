package com.ballsaas.court;

public record CourtResponse(Long id, Long venueId, String name, String sportType, CourtStatus status) {

    static CourtResponse from(Court court) {
        return new CourtResponse(court.getId(), court.getVenueId(), court.getName(), court.getSportType(), court.getStatus());
    }
}

