package com.ballsaas.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ballsaas.booking.BookingOrder;
import com.ballsaas.booking.BookingService;
import com.ballsaas.common.BusinessException;
import com.ballsaas.match.MatchPlayer;
import com.ballsaas.match.MatchPlayerRepository;
import com.ballsaas.match.MatchPlayerStatus;
import com.ballsaas.match.MatchRoom;
import com.ballsaas.match.MatchRoomRepository;
import com.ballsaas.match.MatchStatus;
import com.ballsaas.match.share.MatchSharePayment;
import com.ballsaas.match.share.MatchSharePaymentRepository;
import com.ballsaas.match.share.MatchSharePaymentStatus;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentOrderRepository paymentOrderRepository;

    @Mock
    private PaymentNotifyLogRepository notifyLogRepository;

    @Mock
    private com.ballsaas.booking.BookingOrderRepository bookingOrderRepository;

    @Mock
    private BookingService bookingService;

    @Mock
    private MatchSharePaymentRepository sharePaymentRepository;

    @Mock
    private MatchRoomRepository matchRoomRepository;

    @Mock
    private MatchPlayerRepository matchPlayerRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void prepayBookingForUserRejectsOtherUsersBooking() {
        BookingOrder booking = new BookingOrder(
                "B9001",
                3L,
                5L,
                88L,
                OffsetDateTime.now().plusDays(1),
                OffsetDateTime.now().plusDays(1).plusHours(1),
                6000,
                "lock-token"
        );
        ReflectionTestUtils.setField(booking, "id", 9L);
        when(bookingOrderRepository.findById(9L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> paymentService.prepayBookingForUser(9L, 99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不能支付他人预约订单");

        verify(paymentOrderRepository, never()).save(any(PaymentOrder.class));
    }
    @Test
    void repeatedMockPaySuccessOnlyConfirmsBookingOnce() {
        PaymentOrder paymentOrder = new PaymentOrder("P1001", BusinessType.BOOKING, 9L, 3L, 88L, 6000);
        when(paymentOrderRepository.findByPaymentNo("P1001")).thenReturn(Optional.of(paymentOrder));
        when(paymentOrderRepository.save(any(PaymentOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentOrder first = paymentService.mockPaySuccess("P1001");
        PaymentOrder second = paymentService.mockPaySuccess("P1001");

        assertThat(first.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(second.getStatus()).isEqualTo(PaymentStatus.PAID);
        verify(bookingService, times(1)).confirmPaid(any(Long.class), any());
        verify(notifyLogRepository, times(2)).save(any(PaymentNotifyLog.class));
    }

    @Test
    void mockPaySuccessDoesNotMarkPaymentWhenBookingConfirmationFails() {
        PaymentOrder paymentOrder = new PaymentOrder("P_TIMEOUT", BusinessType.BOOKING, 9L, 3L, 88L, 6000);
        when(paymentOrderRepository.findByPaymentNo("P_TIMEOUT")).thenReturn(Optional.of(paymentOrder));
        doThrow(new BusinessException("当前订单状态不能确认支付"))
                .when(bookingService).confirmPaid(eq(9L), any());

        assertThatThrownBy(() -> paymentService.mockPaySuccess("P_TIMEOUT"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("当前订单状态不能确认支付");

        assertThat(paymentOrder.getStatus()).isEqualTo(PaymentStatus.PENDING_PAYMENT);
        verify(paymentOrderRepository, never()).save(any(PaymentOrder.class));
        verify(notifyLogRepository).save(any(PaymentNotifyLog.class));
    }

    @Test
    void matchSharePaymentFormsRoomWhenPaidCountReachesMinPlayersIncludingCreator() {
        PaymentOrder paymentOrder = new PaymentOrder("MS1001", BusinessType.MATCH_SHARE, 55L, 7L, 88L, 3000);
        ReflectionTestUtils.setField(paymentOrder, "id", 22L);
        MatchSharePayment sharePayment = new MatchSharePayment(5L, 88L, 3000);
        ReflectionTestUtils.setField(sharePayment, "id", 55L);
        sharePayment.attachPaymentOrder(22L, "MS1001");
        MatchPlayer player = new MatchPlayer(5L, 88L);
        MatchRoom room = new MatchRoom("M5", 11L, 7L, 3L, 66L, OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(1), 2, 4, 12000);
        ReflectionTestUtils.setField(room, "id", 5L);
        when(paymentOrderRepository.findByPaymentNo("MS1001")).thenReturn(Optional.of(paymentOrder));
        when(paymentOrderRepository.save(any(PaymentOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(sharePaymentRepository.findByPaymentOrderId(22L)).thenReturn(Optional.of(sharePayment));
        when(matchPlayerRepository.findByMatchRoomIdAndUserId(5L, 88L)).thenReturn(Optional.of(player));
        when(matchRoomRepository.findById(5L)).thenReturn(Optional.of(room));
        when(sharePaymentRepository.countByMatchRoomIdAndStatus(5L, MatchSharePaymentStatus.PAID)).thenReturn(1L);

        paymentService.mockPaySuccess("MS1001");

        assertThat(sharePayment.getStatus()).isEqualTo(MatchSharePaymentStatus.PAID);
        assertThat(player.getStatus()).isEqualTo(MatchPlayerStatus.PAID);
        assertThat(room.getStatus()).isEqualTo(MatchStatus.FORMED);
        verify(matchRoomRepository).save(room);
    }
}

