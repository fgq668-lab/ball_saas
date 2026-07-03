package com.ballsaas.venue;

public record VenueResponse(Long id, String name, String sportTypes, String address, VenueStatus status) {

    static VenueResponse from(Venue venue) {
        return new VenueResponse(venue.getId(), venue.getName(), venue.getSportTypes(), venue.getAddress(), venue.getStatus());
    }
}

