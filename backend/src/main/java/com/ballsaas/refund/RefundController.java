package com.ballsaas.refund;

import com.ballsaas.common.ApiResponse;
import com.ballsaas.security.RequestUserContext;
import com.ballsaas.tenant.RequestVenueContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RefundController {

    private final RefundService refundService;
    private final RefundOrderRepository refundOrderRepository;
    private final RequestVenueContext venueContext;
    private final RequestUserContext userContext;

    public RefundController(
            RefundService refundService,
            RefundOrderRepository refundOrderRepository,
            RequestVenueContext venueContext,
            RequestUserContext userContext) {
        this.refundService = refundService;
        this.refundOrderRepository = refundOrderRepository;
        this.venueContext = venueContext;
        this.userContext = userContext;
    }

    @PostMapping("/api/app/refunds")
    public ApiResponse<RefundResponse> create(@Valid @RequestBody CreateRefundRequest request, HttpServletRequest servletRequest) {
        return userContext.currentUserId(servletRequest)
                .map(userId -> ApiResponse.ok(RefundResponse.from(refundService.createForUser(request, userId))))
                .orElseGet(() -> ApiResponse.ok(RefundResponse.from(refundService.create(request))));
    }

    @GetMapping("/api/venue/refunds")
    public ApiResponse<List<RefundResponse>> listVenueRefunds(HttpServletRequest request) {
        Long venueId = venueContext.requireVenueId(request);
        return ApiResponse.ok(refundOrderRepository.findByVenueIdOrderByCreatedAtDesc(venueId).stream().map(RefundResponse::from).toList());
    }

    @PostMapping("/api/pay/mock/refund/success/{refundNo}")
    public ApiResponse<RefundResponse> mockRefundSuccess(@PathVariable String refundNo) {
        return ApiResponse.ok(RefundResponse.from(refundService.mockRefundSuccess(refundNo)));
    }

    @PostMapping("/api/pay/wechat/notify/refund")
    public ApiResponse<String> wechatRefundNotify(@RequestBody String rawBody) {
        refundService.recordMockWechatRefundNotify(rawBody);
        return ApiResponse.ok("mock-refund-notify-accepted");
    }
}
