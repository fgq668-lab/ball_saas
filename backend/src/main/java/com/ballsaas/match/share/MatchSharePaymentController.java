package com.ballsaas.match.share;

import com.ballsaas.common.ApiResponse;
import com.ballsaas.security.RequestUserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/matches")
public class MatchSharePaymentController {

    private final MatchSharePaymentService sharePaymentService;
    private final MatchSharePaymentRepository sharePaymentRepository;
    private final MatchShareRefundRepository shareRefundRepository;
    private final RequestUserContext userContext;

    public MatchSharePaymentController(
            MatchSharePaymentService sharePaymentService,
            MatchSharePaymentRepository sharePaymentRepository,
            MatchShareRefundRepository shareRefundRepository,
            RequestUserContext userContext) {
        this.sharePaymentService = sharePaymentService;
        this.sharePaymentRepository = sharePaymentRepository;
        this.shareRefundRepository = shareRefundRepository;
        this.userContext = userContext;
    }

    @PostMapping("/share-payments")
    public ApiResponse<SharePaymentResponse> create(@Valid @RequestBody CreateSharePaymentRequest request, HttpServletRequest servletRequest) {
        Long userId = userContext.resolveUserId(servletRequest, request.userId());
        CreateSharePaymentRequest command = new CreateSharePaymentRequest(request.matchRoomId(), userId, request.amountCent());
        return ApiResponse.ok(SharePaymentResponse.from(sharePaymentService.create(command)));
    }

    @PostMapping("/{matchId}/share-payments")
    public ApiResponse<SharePaymentResponse> createForMatch(@PathVariable Long matchId, @Valid @RequestBody CreateSharePaymentForMatchRequest request, HttpServletRequest servletRequest) {
        Long userId = userContext.resolveUserId(servletRequest, request.userId());
        CreateSharePaymentRequest command = new CreateSharePaymentRequest(matchId, userId, request.amountCent());
        return ApiResponse.ok(SharePaymentResponse.from(sharePaymentService.create(command)));
    }

    @GetMapping("/{matchId}/share-payments")
    public ApiResponse<List<SharePaymentResponse>> listForMatch(@PathVariable Long matchId) {
        return ApiResponse.ok(sharePaymentRepository.findByMatchRoomId(matchId).stream().map(SharePaymentResponse::from).toList());
    }

    @GetMapping("/{matchId}/share-refunds")
    public ApiResponse<List<ShareRefundResponse>> listRefundsForMatch(@PathVariable Long matchId) {
        return ApiResponse.ok(shareRefundRepository.findByMatchRoomId(matchId).stream().map(ShareRefundResponse::from).toList());
    }
}
