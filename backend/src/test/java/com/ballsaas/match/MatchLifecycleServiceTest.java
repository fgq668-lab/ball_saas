package com.ballsaas.match;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ballsaas.match.share.MatchSharePayment;
import com.ballsaas.match.share.MatchSharePaymentRepository;
import com.ballsaas.match.share.MatchSharePaymentStatus;
import com.ballsaas.match.share.MatchShareRefund;
import com.ballsaas.match.share.MatchShareRefundRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MatchLifecycleServiceTest {

    @Mock
    private MatchRoomRepository matchRoomRepository;

    @Mock
    private MatchSharePaymentRepository sharePaymentRepository;

    @Mock
    private MatchShareRefundRepository shareRefundRepository;

    @InjectMocks
    private MatchLifecycleService lifecycleService;

    @Test
    void cancelsExpiredUnformedMatchAndCreatesRefundRecord() {
        MatchRoom room = matchRoom(7L, 3, 4);
        MatchSharePayment payment = new MatchSharePayment(7L, 88L, 3000);
        payment.markPaid();
        when(matchRoomRepository.findByStatusInAndStartAtBefore(anyCollection(), any())).thenReturn(List.of(room));
        when(sharePaymentRepository.countByMatchRoomIdAndStatus(7L, MatchSharePaymentStatus.PAID)).thenReturn(1L);
        when(sharePaymentRepository.findByMatchRoomId(7L)).thenReturn(List.of(payment));

        int cancelled = lifecycleService.cancelUnformedMatches();

        assertThat(cancelled).isEqualTo(1);
        assertThat(room.getStatus()).isEqualTo(MatchStatus.NOT_FORMED_CANCELLED);
        assertThat(payment.getStatus()).isEqualTo(MatchSharePaymentStatus.REFUNDED);
        ArgumentCaptor<MatchShareRefund> captor = ArgumentCaptor.forClass(MatchShareRefund.class);
        verify(shareRefundRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("REFUNDED");
    }

    @Test
    void doesNotCancelWhenCreatorAndPaidSharesReachMinPlayers() {
        MatchRoom room = matchRoom(8L, 2, 4);
        when(matchRoomRepository.findByStatusInAndStartAtBefore(anyCollection(), any())).thenReturn(List.of(room));
        when(sharePaymentRepository.countByMatchRoomIdAndStatus(8L, MatchSharePaymentStatus.PAID)).thenReturn(1L);

        int cancelled = lifecycleService.cancelUnformedMatches();

        assertThat(cancelled).isZero();
        assertThat(room.getStatus()).isEqualTo(MatchStatus.RECRUITING);
        verify(matchRoomRepository, never()).save(any(MatchRoom.class));
        verify(sharePaymentRepository, never()).findByMatchRoomId(anyLong());
    }

    private MatchRoom matchRoom(Long id, int minPlayers, int maxPlayers) {
        MatchRoom room = new MatchRoom(
                "M" + id,
                11L,
                7L,
                3L,
                66L,
                OffsetDateTime.now().minusHours(1),
                OffsetDateTime.now(),
                minPlayers,
                maxPlayers,
                12000
        );
        ReflectionTestUtils.setField(room, "id", id);
        return room;
    }
}
