package com.ballsaas.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.ballsaas.booking.BookingOrderRepository;
import com.ballsaas.booking.BookingStatus;
import com.ballsaas.refund.RefundOrderRepository;
import com.ballsaas.refund.RefundStatus;
import com.ballsaas.venue.VenueRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private BookingOrderRepository bookingOrderRepository;

    @Mock
    private RefundOrderRepository refundOrderRepository;

    @Mock
    private VenueRepository venueRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void platformDashboardUsesRealAmountSums() {
        when(bookingOrderRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(3L);
        when(bookingOrderRepository.sumPayableCentByStatusesAndCreatedAtBetween(any(List.class), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(18000);
        when(refundOrderRepository.sumAmountCentByStatusAndCreatedAtBetween(eq(RefundStatus.REFUNDED), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(3000);
        when(refundOrderRepository.countByStatus(RefundStatus.REQUESTED)).thenReturn(2L);
        when(refundOrderRepository.countByStatus(RefundStatus.PROCESSING)).thenReturn(1L);
        when(venueRepository.count()).thenReturn(5L);

        DashboardMetric metric = reportService.platformDashboard();

        assertThat(metric.todayOrders()).isEqualTo(3);
        assertThat(metric.todayAmountCent()).isEqualTo(18000);
        assertThat(metric.todayRefundCent()).isEqualTo(3000);
        assertThat(metric.pendingRefunds()).isEqualTo(3);
        assertThat(metric.venues()).isEqualTo(5);
    }

    @Test
    void venueDashboardScopesAmountsByVenue() {
        when(bookingOrderRepository.countByVenueIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(eq(7L), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(2L);
        when(bookingOrderRepository.sumPayableCentByVenueIdAndStatusesAndCreatedAtBetween(eq(7L), any(List.class), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(12000);
        when(refundOrderRepository.sumAmountCentByVenueIdAndStatusAndCreatedAtBetween(eq(7L), eq(RefundStatus.REFUNDED), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(1000);
        when(refundOrderRepository.countByVenueIdAndStatus(7L, RefundStatus.REQUESTED)).thenReturn(1L);
        when(refundOrderRepository.countByVenueIdAndStatus(7L, RefundStatus.PROCESSING)).thenReturn(0L);

        DashboardMetric metric = reportService.venueDashboard(7L);

        assertThat(metric.todayOrders()).isEqualTo(2);
        assertThat(metric.todayAmountCent()).isEqualTo(12000);
        assertThat(metric.todayRefundCent()).isEqualTo(1000);
        assertThat(metric.pendingRefunds()).isEqualTo(1);
    }
}
