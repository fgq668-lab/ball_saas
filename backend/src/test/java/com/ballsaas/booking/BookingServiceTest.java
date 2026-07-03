package com.ballsaas.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ballsaas.common.BusinessException;
import com.ballsaas.pricing.PriceService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingOrderRepository bookingOrderRepository;

    @Mock
    private SlotService slotService;

    @Mock
    private PriceService priceService;

    @Mock
    private StringRedisTemplate redisTemplate;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(bookingOrderRepository, slotService, priceService, redisTemplate, "test", 10);
    }

    @Test
    void closeTimeoutOrdersOnlyScansPendingPaymentOrders() {
        OffsetDateTime now = OffsetDateTime.now();
        BookingOrder pending = booking();
        when(bookingOrderRepository.findByStatusAndCreatedAtBefore(eq(BookingStatus.PENDING_PAYMENT), any()))
                .thenReturn(List.of(pending));
        when(bookingOrderRepository.save(any(BookingOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        int closed = bookingService.closeTimeoutOrders(now);

        assertThat(closed).isEqualTo(1);
        assertThat(pending.getStatus()).isEqualTo(BookingStatus.PAYMENT_TIMEOUT);
        verify(bookingOrderRepository).findByStatusAndCreatedAtBefore(eq(BookingStatus.PENDING_PAYMENT), any());
        verify(bookingOrderRepository).save(pending);
    }

    @Test
    void confirmPaidRejectsTimedOutBooking() {
        BookingOrder order = booking();
        order.markPaymentTimeout();
        when(bookingOrderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> bookingService.confirmPaid(10L, OffsetDateTime.now()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("当前订单状态不能确认支付");

        assertThat(order.getStatus()).isEqualTo(BookingStatus.PAYMENT_TIMEOUT);
        verify(slotService, never()).assertAvailable(any(), any(), any(), any());
        verify(slotService, never()).createSlots(any());
        verify(bookingOrderRepository, never()).save(any(BookingOrder.class));
    }

    @Test
    void confirmPaidKeepsReservedBookingIdempotent() {
        BookingOrder order = booking();
        order.markReserved(OffsetDateTime.now().minusMinutes(1));
        when(bookingOrderRepository.findById(10L)).thenReturn(Optional.of(order));

        BookingOrder result = bookingService.confirmPaid(10L, OffsetDateTime.now());

        assertThat(result).isSameAs(order);
        assertThat(order.getStatus()).isEqualTo(BookingStatus.RESERVED);
        verify(slotService, never()).assertAvailable(any(), any(), any(), any());
        verify(slotService, never()).createSlots(any());
        verify(bookingOrderRepository, never()).save(any(BookingOrder.class));
    }

    private BookingOrder booking() {
        OffsetDateTime startAt = OffsetDateTime.now().plusDays(1);
        return new BookingOrder(
                "B1001",
                1L,
                2L,
                3L,
                startAt,
                startAt.plusHours(1),
                12000,
                "lock-token"
        );
    }
}
