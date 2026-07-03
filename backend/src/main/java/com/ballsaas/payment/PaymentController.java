package com.ballsaas.payment;

import com.ballsaas.common.ApiResponse;
import com.ballsaas.security.RequestUserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

    private final PaymentService paymentService;
    private final RequestUserContext userContext;

    public PaymentController(PaymentService paymentService, RequestUserContext userContext) {
        this.paymentService = paymentService;
        this.userContext = userContext;
    }

    @PostMapping("/api/app/payments/prepay")
    public ApiResponse<PaymentResponse> prepay(@Valid @RequestBody PrepayRequest request, HttpServletRequest servletRequest) {
        return userContext.currentUserId(servletRequest)
                .map(userId -> ApiResponse.ok(PaymentResponse.from(paymentService.prepayBookingForUser(request.bookingId(), userId))))
                .orElseGet(() -> ApiResponse.ok(PaymentResponse.from(paymentService.prepayBooking(request.bookingId()))));
    }

    @PostMapping("/api/pay/mock/success/{paymentNo}")
    public ApiResponse<PaymentResponse> mockSuccess(@PathVariable String paymentNo) {
        return ApiResponse.ok(PaymentResponse.from(paymentService.mockPaySuccess(paymentNo)));
    }

    @PostMapping("/api/pay/wechat/notify/payment")
    public ApiResponse<String> wechatPaymentNotify(@RequestBody String rawBody) {
        paymentService.recordMockWechatNotify(rawBody);
        return ApiResponse.ok("mock-notify-accepted");
    }
}
