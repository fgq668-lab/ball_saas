package com.ballsaas.payment;

public record PaymentResponse(Long id, String paymentNo, PaymentStatus status, String mockPayUrl) {

    static PaymentResponse from(PaymentOrder order) {
        return new PaymentResponse(order.getId(), order.getPaymentNo(), order.getStatus(), "/api/pay/mock/success/" + order.getPaymentNo());
    }
}

