package com.ballsaas.booking;

import com.ballsaas.common.ApiResponse;
import com.ballsaas.payment.BusinessType;
import com.ballsaas.payment.PaymentOrderRepository;
import com.ballsaas.security.RequestUserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final PaymentOrderRepository paymentOrderRepository;
    private final RequestUserContext userContext;

    public BookingController(BookingService bookingService, PaymentOrderRepository paymentOrderRepository, RequestUserContext userContext) {
        this.bookingService = bookingService;
        this.paymentOrderRepository = paymentOrderRepository;
        this.userContext = userContext;
    }

    @PostMapping
    public ApiResponse<BookingResponse> create(@Valid @RequestBody CreateBookingRequest request, HttpServletRequest servletRequest) {
        Long userId = userContext.resolveUserId(servletRequest, request.userId());
        CreateBookingRequest command = new CreateBookingRequest(
                request.venueId(),
                request.courtId(),
                userId,
                request.startAt(),
                request.endAt()
        );
        return ApiResponse.ok(BookingResponse.from(bookingService.create(command)));
    }

    @GetMapping
    public ApiResponse<List<BookingResponse>> list(@RequestParam(required = false) Long userId, HttpServletRequest request) {
        Long scopedUserId = userContext.resolveUserId(request, userId);
        return ApiResponse.ok(bookingService.listByUser(scopedUserId).stream().map(this::withPayment).toList());
    }

    @GetMapping("/{bookingId}")
    public ApiResponse<BookingResponse> detail(@PathVariable Long bookingId, @RequestParam(required = false) Long userId, HttpServletRequest request) {
        Long scopedUserId = userContext.resolveUserId(request, userId);
        return ApiResponse.ok(withPayment(bookingService.detailForUser(bookingId, scopedUserId)));
    }

    @PostMapping("/{bookingId}/cancel")
    public ApiResponse<BookingResponse> cancel(@PathVariable Long bookingId, HttpServletRequest request) {
        return userContext.currentUserId(request)
                .map(userId -> ApiResponse.ok(withPayment(bookingService.cancelForUser(bookingId, userId))))
                .orElseGet(() -> ApiResponse.ok(withPayment(bookingService.cancel(bookingId))));
    }

    private BookingResponse withPayment(BookingOrder order) {
        return BookingResponse.from(order, paymentOrderRepository.findFirstByBusinessTypeAndBusinessIdOrderByCreatedAtDesc(BusinessType.BOOKING, order.getId()).orElse(null));
    }
}
