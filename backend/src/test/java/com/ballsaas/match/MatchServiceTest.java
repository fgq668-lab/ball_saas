package com.ballsaas.match;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ballsaas.booking.BookingOrderRepository;
import com.ballsaas.match.share.MatchSharePayment;
import com.ballsaas.match.share.MatchSharePaymentRepository;
import com.ballsaas.match.share.MatchSharePaymentStatus;
import com.ballsaas.match.share.MatchShareRefund;
import com.ballsaas.match.share.MatchShareRefundRepository;
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
class MatchServiceTest {

    @Mock
    private MatchRoomRepository matchRoomRepository;

    @Mock
    private MatchPlayerRepository matchPlayerRepository;

    @Mock
    private BookingOrderRepository bookingOrderRepository;

    @Mock
    private MatchSharePaymentRepository sharePaymentRepository;

    @Mock
    private MatchShareRefundRepository shareRefundRepository;

    @InjectMocks
    private MatchService matchService;

    @Test
    void cancelNotFormedCreatesRefundRecordsForPaidSharePayments() {
        MatchRoom room = matchRoom(5L, 2, 4);
        MatchSharePayment payment = new MatchSharePayment(5L, 88L, 3000);
        ReflectionTestUtils.setField(payment, "id", 9L);
        payment.markPaid();
        when(matchRoomRepository.findById(5L)).thenReturn(Optional.of(room));
        when(sharePaymentRepository.findByMatchRoomId(5L)).thenReturn(List.of(payment));
        when(matchRoomRepository.save(any(MatchRoom.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MatchRoom cancelled = matchService.cancelNotFormed(5L);

        assertThat(cancelled.getStatus()).isEqualTo(MatchStatus.NOT_FORMED_CANCELLED);
        assertThat(payment.getStatus()).isEqualTo(MatchSharePaymentStatus.REFUNDED);
        ArgumentCaptor<MatchShareRefund> captor = ArgumentCaptor.forClass(MatchShareRefund.class);
        verify(shareRefundRepository).save(captor.capture());
        assertThat(captor.getValue().getMatchRoomId()).isEqualTo(5L);
        assertThat(captor.getValue().getUserId()).isEqualTo(88L);
        assertThat(captor.getValue().getAmountCent()).isEqualTo(3000);
        assertThat(captor.getValue().getStatus()).isEqualTo("REFUNDED");
    }

    @Test
    void memberLeaveCreatesRefundRecordForPaidSharePayment() {
        MatchRoom room = matchRoom(6L, 2, 4);
        room.playerJoined();
        MatchPlayer player = new MatchPlayer(6L, 89L);
        MatchSharePayment payment = new MatchSharePayment(6L, 89L, 3000);
        payment.markPaid();
        when(matchRoomRepository.findById(6L)).thenReturn(Optional.of(room));
        when(matchPlayerRepository.findByMatchRoomIdAndUserId(6L, 89L)).thenReturn(Optional.of(player));
        when(sharePaymentRepository.findByMatchRoomIdAndUserId(6L, 89L)).thenReturn(Optional.of(payment));
        when(matchRoomRepository.save(any(MatchRoom.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MatchRoom afterLeave = matchService.leave(6L, new JoinMatchRequest(89L));

        assertThat(afterLeave.getCurrentPlayers()).isEqualTo(1);
        assertThat(player.getStatus()).isEqualTo(MatchPlayerStatus.LEFT);
        assertThat(payment.getStatus()).isEqualTo(MatchSharePaymentStatus.REFUNDED);
        verify(shareRefundRepository).save(any(MatchShareRefund.class));
    }

    private MatchRoom matchRoom(Long id, int minPlayers, int maxPlayers) {
        MatchRoom room = new MatchRoom(
                "M" + id,
                11L,
                7L,
                3L,
                66L,
                OffsetDateTime.now().plusDays(1),
                OffsetDateTime.now().plusDays(1).plusHours(1),
                minPlayers,
                maxPlayers,
                12000
        );
        ReflectionTestUtils.setField(room, "id", id);
        return room;
    }
}
