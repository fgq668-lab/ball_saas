package com.ballsaas.report;

import com.ballsaas.booking.BookingOrderRepository;
import com.ballsaas.booking.BookingStatus;
import com.ballsaas.refund.RefundOrderRepository;
import com.ballsaas.refund.RefundStatus;
import com.ballsaas.venue.VenueRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private static final List<BookingStatus> PAID_BOOKING_STATUSES = List.of(
            BookingStatus.RESERVED,
            BookingStatus.CHECKED_IN,
            BookingStatus.COMPLETED,
            BookingStatus.REFUNDING,
            BookingStatus.PARTIALLY_REFUNDED,
            BookingStatus.REFUNDED
    );

    private final BookingOrderRepository bookingOrderRepository;
    private final RefundOrderRepository refundOrderRepository;
    private final VenueRepository venueRepository;

    public ReportService(BookingOrderRepository bookingOrderRepository, RefundOrderRepository refundOrderRepository, VenueRepository venueRepository) {
        this.bookingOrderRepository = bookingOrderRepository;
        this.refundOrderRepository = refundOrderRepository;
        this.venueRepository = venueRepository;
    }

    public DashboardMetric platformDashboard() {
        DateRange today = todayRange();
        return new DashboardMetric(
                bookingOrderRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(today.startAt(), today.endAt()),
                bookingOrderRepository.sumPayableCentByStatusesAndCreatedAtBetween(PAID_BOOKING_STATUSES, today.startAt(), today.endAt()),
                refundOrderRepository.sumAmountCentByStatusAndCreatedAtBetween(RefundStatus.REFUNDED, today.startAt(), today.endAt()),
                refundOrderRepository.countByStatus(RefundStatus.REQUESTED) + refundOrderRepository.countByStatus(RefundStatus.PROCESSING),
                venueRepository.count()
        );
    }

    public DashboardMetric venueDashboard(Long venueId) {
        DateRange today = todayRange();
        return new DashboardMetric(
                bookingOrderRepository.countByVenueIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(venueId, today.startAt(), today.endAt()),
                bookingOrderRepository.sumPayableCentByVenueIdAndStatusesAndCreatedAtBetween(venueId, PAID_BOOKING_STATUSES, today.startAt(), today.endAt()),
                refundOrderRepository.sumAmountCentByVenueIdAndStatusAndCreatedAtBetween(venueId, RefundStatus.REFUNDED, today.startAt(), today.endAt()),
                refundOrderRepository.countByVenueIdAndStatus(venueId, RefundStatus.REQUESTED) + refundOrderRepository.countByVenueIdAndStatus(venueId, RefundStatus.PROCESSING),
                1
        );
    }

    private DateRange todayRange() {
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        OffsetDateTime startAt = today.atStartOfDay(BUSINESS_ZONE).toOffsetDateTime();
        OffsetDateTime endAt = today.plusDays(1).atStartOfDay(BUSINESS_ZONE).toOffsetDateTime();
        return new DateRange(startAt, endAt);
    }

    private record DateRange(OffsetDateTime startAt, OffsetDateTime endAt) {
    }
}