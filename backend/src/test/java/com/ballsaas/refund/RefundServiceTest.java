package com.ballsaas.refund;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ballsaas.booking.BookingOrder;
import com.ballsaas.booking.BookingOrderRepository;
import com.ballsaas.booking.BookingStatus;
import com.ballsaas.booking.CourtTimeSlotRepository;
import com.ballsaas.common.BusinessException;
import com.ballsaas.payment.BusinessType;
import com.ballsaas.payment.PaymentNotifyLog;
import com.ballsaas.payment.PaymentNotifyLogRepository;
import com.ballsaas.payment.PaymentOrder;
import com.ballsaas.payment.PaymentOrderRepository;
import com.ballsaas.payment.PaymentStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {

    @Mock
    private RefundOrderRepository refundOrderRepository;

    @Mock
    private PaymentOrderRepository paymentOrderRepository;

    @Mock
    private BookingOrderRepository bookingOrderRepository;

    @Mock
    private CourtTimeSlotRepository slotRepository;

    @Mock
    private PaymentNotifyLogRepository notifyLogRepository;

    @InjectMocks
    private RefundService refundService;

    @Test
    void fullRefundForFutureBookingReleasesSlots() {
        PaymentOrder payment = paidBookingPayment(1L, 10L, 6000);
        BookingOrder booking = reservedBooking(10L, 6000, OffsetDateTime.now().plusDays(2), OffsetDateTime.now().plusDays(2).plusHours(1));
        when(paymentOrderRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(refundOrderRepository.findByPaymentOrderId(1L)).thenReturn(List.of());
        when(refundOrderRepository.save(any(RefundOrder.class))).thenAnswer(invocation -> {
            RefundOrder order = invocation.getArgument(0);
            if (order.getId() == null) {
                ReflectionTestUtils.setField(order, "id", 99L);
            }
            return order;
        });
        when(bookingOrderRepository.findById(10L)).thenReturn(Optional.of(booking));

        RefundOrder refund = refundService.create(new CreateRefundRequest(1L, 7L, 6000, "用户取消"));

        assertThat(refund.getStatus()).isEqualTo(RefundStatus.REFUNDED);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.REFUNDED);
        verify(slotRepository).deleteByBookingOrderId(10L);
        verify(notifyLogRepository).save(any(PaymentNotifyLog.class));
    }

    @Test
    void partialRefundDoesNotReleaseSlots() {
        PaymentOrder payment = paidBookingPayment(1L, 10L, 6000);
        BookingOrder booking = reservedBooking(10L, 6000, OffsetDateTime.now().plusDays(2), OffsetDateTime.now().plusDays(2).plusHours(1));
        when(paymentOrderRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(refundOrderRepository.findByPaymentOrderId(1L)).thenReturn(List.of());
        when(refundOrderRepository.save(any(RefundOrder.class))).thenAnswer(invocation -> {
            RefundOrder order = invocation.getArgument(0);
            if (order.getId() == null) {
                ReflectionTestUtils.setField(order, "id", 99L);
            }
            return order;
        });
        when(bookingOrderRepository.findById(10L)).thenReturn(Optional.of(booking));

        refundService.create(new CreateRefundRequest(1L, 7L, 3000, "部分退款"));

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PARTIALLY_REFUNDED);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PARTIALLY_REFUNDED);
        verify(slotRepository, never()).deleteByBookingOrderId(any());
        verify(notifyLogRepository).save(any(PaymentNotifyLog.class));
    }

    @Test
    void rejectsOverRefund() {
        PaymentOrder payment = paidBookingPayment(1L, 10L, 6000);
        RefundOrder existing = new RefundOrder("R1", 1L, 7L, 4000, "已退");
        existing.markRefunded("MOCK-R1");
        when(paymentOrderRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(refundOrderRepository.findByPaymentOrderId(1L)).thenReturn(List.of(existing));

        assertThatThrownBy(() -> refundService.create(new CreateRefundRequest(1L, 7L, 3000, "超额退款")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("超过可退金额");
    }

    @Test
    void createForUserRejectsOtherUsersPayment() {
        PaymentOrder payment = paidBookingPayment(1L, 10L, 6000);
        when(paymentOrderRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> refundService.createForUser(new CreateRefundRequest(1L, 7L, 3000, "冒用退款"), 99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不能申请他人支付单退款");

        verify(refundOrderRepository, never()).findByPaymentOrderId(any());
        verify(refundOrderRepository, never()).save(any(RefundOrder.class));
        verify(paymentOrderRepository, never()).save(any(PaymentOrder.class));
    }
    @Test
    void repeatedRefundSuccessCallbackOnlyWritesDuplicateLog() {
        RefundOrder existing = new RefundOrder("R1001", 1L, 7L, 6000, "重复通知");
        ReflectionTestUtils.setField(existing, "id", 99L);
        existing.markRefunded("MOCK-REFUND-99");
        when(refundOrderRepository.findByRefundNo("R1001")).thenReturn(Optional.of(existing));

        RefundOrder first = refundService.mockRefundSuccess("R1001");
        RefundOrder second = refundService.mockRefundSuccess("R1001");

        assertThat(first.getStatus()).isEqualTo(RefundStatus.REFUNDED);
        assertThat(second.getStatus()).isEqualTo(RefundStatus.REFUNDED);
        ArgumentCaptor<PaymentNotifyLog> captor = ArgumentCaptor.forClass(PaymentNotifyLog.class);
        verify(notifyLogRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
                .allSatisfy(log -> {
                    assertThat(log.getNotifyType()).isEqualTo("REFUND_SUCCESS");
                    assertThat(log.getOutTradeNo()).isEqualTo("R1001");
                    assertThat(log.getProcessStatus()).isEqualTo("DUPLICATE");
                });
        verify(paymentOrderRepository, never()).save(any(PaymentOrder.class));
        verify(slotRepository, never()).deleteByBookingOrderId(any());
    }

    private PaymentOrder paidBookingPayment(Long paymentId, Long bookingId, int amountCent) {
        PaymentOrder payment = new PaymentOrder("P" + paymentId, BusinessType.BOOKING, bookingId, 7L, 88L, amountCent);
        ReflectionTestUtils.setField(payment, "id", paymentId);
        payment.markPaid("MOCK", OffsetDateTime.now());
        return payment;
    }

    private BookingOrder reservedBooking(Long bookingId, int amountCent, OffsetDateTime startAt, OffsetDateTime endAt) {
        BookingOrder booking = new BookingOrder("B" + bookingId, 7L, 3L, 88L, startAt, endAt, amountCent, "lock");
        ReflectionTestUtils.setField(booking, "id", bookingId);
        booking.markReserved(OffsetDateTime.now());
        return booking;
    }
}

