package com.ballsaas.booking;

public enum BookingStatus {
    PENDING_PAYMENT,
    RESERVED,
    CHECKED_IN,
    COMPLETED,
    CANCELLED,
    REFUNDING,
    PARTIALLY_REFUNDED,
    REFUNDED,
    PAYMENT_TIMEOUT,
    MANUAL_REVIEW
}

