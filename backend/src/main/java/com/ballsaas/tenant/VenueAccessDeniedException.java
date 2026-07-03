package com.ballsaas.tenant;

public class VenueAccessDeniedException extends RuntimeException {

    public VenueAccessDeniedException(String message) {
        super(message);
    }
}

