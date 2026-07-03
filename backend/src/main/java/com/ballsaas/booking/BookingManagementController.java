package com.ballsaas.booking;

import com.ballsaas.audit.AuditService;
import com.ballsaas.common.ApiResponse;
import com.ballsaas.common.BusinessException;
import com.ballsaas.payment.BusinessType;
import com.ballsaas.payment.PaymentOrderRepository;
import com.ballsaas.tenant.RequestVenueContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/venue/bookings")
public class BookingManagementController {

    private final BookingOrderRepository bookingOrderRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final RequestVenueContext venueContext;
    private final AuditService auditService;

    public BookingManagementController(
            BookingOrderRepository bookingOrderRepository,
            PaymentOrderRepository paymentOrderRepository,
            RequestVenueContext venueContext,
            AuditService auditService) {
        this.bookingOrderRepository = bookingOrderRepository;
        this.paymentOrderRepository = paymentOrderRepository;
        this.venueContext = venueContext;
        this.auditService = auditService;
    }

    @GetMapping
    public ApiResponse<List<BookingResponse>> list(HttpServletRequest request) {
        Long scopedVenueId = venueContext.requireVenueId(request);
        return ApiResponse.ok(bookingOrderRepository.findByVenueIdOrderByCreatedAtDesc(scopedVenueId).stream().map(this::withPayment).toList());
    }

    @GetMapping("/{bookingId}")
    public ApiResponse<BookingResponse> detail(@PathVariable Long bookingId, HttpServletRequest request) {
        BookingOrder order = bookingOrderRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("预约订单不存在"));
        venueContext.assertVenue(order.getVenueId(), request);
        return ApiResponse.ok(withPayment(order));
    }

    @PostMapping("/{bookingId}/checkin")
    public ApiResponse<BookingResponse> checkIn(@PathVariable Long bookingId, HttpServletRequest request) {
        BookingOrder order = bookingOrderRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("预约订单不存在"));
        venueContext.assertVenue(order.getVenueId(), request);
        if (order.getStatus() != BookingStatus.RESERVED) {
            throw new BusinessException("只有已预约订单可以核销");
        }
        order.checkIn();
        BookingOrder saved = bookingOrderRepository.save(order);
        auditService.record("BOOKING_CHECKIN", "BOOKING_ORDER", saved.getId(), saved.getVenueId(), request, "核销预约订单: " + saved.getOrderNo());
        return ApiResponse.ok(withPayment(saved));
    }

    private BookingResponse withPayment(BookingOrder order) {
        return BookingResponse.from(order, paymentOrderRepository.findFirstByBusinessTypeAndBusinessIdOrderByCreatedAtDesc(BusinessType.BOOKING, order.getId()).orElse(null));
    }
}
