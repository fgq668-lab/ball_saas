package com.ballsaas.match.share;

public record ShareRefundResponse(
        Long id,
        Long matchRoomId,
        Long userId,
        Long refundOrderId,
        int amountCent,
        String status
) {

    static ShareRefundResponse from(MatchShareRefund refund) {
        return new ShareRefundResponse(
                refund.getId(),
                refund.getMatchRoomId(),
                refund.getUserId(),
                refund.getRefundOrderId(),
                refund.getAmountCent(),
                refund.getStatus()
        );
    }
}
