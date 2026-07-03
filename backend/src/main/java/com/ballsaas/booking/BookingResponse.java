package com.ballsaas.booking;

import com.ballsaas.payment.PaymentOrder;
import java.time.OffsetDateTime;

public record BookingResponse(
        Long id,
        String orderNo,
        Long venueId,
        Long courtId,
        Long userId,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        int payableCent,
        BookingStatus status,
        Long paymentOrderId,
        String paymentNo,
        String paymentStatus
) {
    static BookingResponse from(BookingOrder order) {
        return from(order, null);
    }

    static BookingResponse from(BookingOrder order, PaymentOrder payment) {
        return new BookingResponse(
                order.getId(),
                order.getOrderNo(),
                order.getVenueId(),
                order.getCourtId(),
                order.getUserId(),
                order.getStartAt(),
                order.getEndAt(),
                order.getPayableCent(),
                order.getStatus(),
                payment == null ? null : payment.getId(),
                payment == null ? null : payment.getPaymentNo(),
                payment == null ? null : payment.getStatus().name()
        );
    }
}
