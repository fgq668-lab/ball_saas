package com.ballsaas.refund;

import java.time.OffsetDateTime;

public record RefundResponse(
        Long id,
        String refundNo,
        Long paymentOrderId,
        Long venueId,
        int amountCent,
        String reason,
        String status,
        OffsetDateTime requestedAt,
        String message
) {

    static RefundResponse from(RefundOrder order) {
        return new RefundResponse(
                order.getId(),
                order.getRefundNo(),
                order.getPaymentOrderId(),
                order.getVenueId(),
                order.getAmountCent(),
                order.getReason(),
                order.getStatus().name(),
                order.getRequestedAt(),
                "退款单已创建"
        );
    }
}