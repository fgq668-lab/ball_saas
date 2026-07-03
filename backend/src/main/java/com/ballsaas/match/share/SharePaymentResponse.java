package com.ballsaas.match.share;

public record SharePaymentResponse(
        Long id,
        Long matchRoomId,
        Long userId,
        Long paymentOrderId,
        String paymentNo,
        int amountCent,
        String status,
        String message
) {

    static SharePaymentResponse from(MatchSharePayment payment) {
        return new SharePaymentResponse(
                payment.getId(),
                payment.getMatchRoomId(),
                payment.getUserId(),
                payment.getPaymentOrderId(),
                payment.getPaymentNo(),
                payment.getAmountCent(),
                payment.getStatus().name(),
                "约战分摊支付单已创建"
        );
    }
}
