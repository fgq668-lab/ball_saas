package com.ballsaas.match;

import com.ballsaas.match.share.MatchSharePayment;
import com.ballsaas.match.share.MatchSharePaymentRepository;
import com.ballsaas.match.share.MatchSharePaymentStatus;
import com.ballsaas.match.share.MatchShareRefund;
import com.ballsaas.match.share.MatchShareRefundRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchLifecycleService {

    private final MatchRoomRepository matchRoomRepository;
    private final MatchSharePaymentRepository sharePaymentRepository;
    private final MatchShareRefundRepository shareRefundRepository;

    public MatchLifecycleService(
            MatchRoomRepository matchRoomRepository,
            MatchSharePaymentRepository sharePaymentRepository,
            MatchShareRefundRepository shareRefundRepository) {
        this.matchRoomRepository = matchRoomRepository;
        this.sharePaymentRepository = sharePaymentRepository;
        this.shareRefundRepository = shareRefundRepository;
    }

    @Transactional
    public int cancelUnformedMatches() {
        List<MatchRoom> rooms = matchRoomRepository.findByStatusInAndStartAtBefore(
                List.of(MatchStatus.RECRUITING, MatchStatus.FULL),
                OffsetDateTime.now()
        );
        int cancelled = 0;
        for (MatchRoom room : rooms) {
            long paidCountIncludingCreator = sharePaymentRepository.countByMatchRoomIdAndStatus(room.getId(), MatchSharePaymentStatus.PAID) + 1;
            if (paidCountIncludingCreator < room.getMinPlayers()) {
                room.cancelNotFormed();
                matchRoomRepository.save(room);
                refundPaidShares(room);
                cancelled++;
            }
        }
        return cancelled;
    }

    private void refundPaidShares(MatchRoom room) {
        List<MatchSharePayment> payments = sharePaymentRepository.findByMatchRoomId(room.getId());
        for (MatchSharePayment payment : payments) {
            if (payment.getStatus() == MatchSharePaymentStatus.PAID) {
                payment.markRefunded();
                sharePaymentRepository.save(payment);
                MatchShareRefund refund = new MatchShareRefund(room.getId(), payment.getUserId(), payment.getAmountCent());
                refund.markRefunded();
                shareRefundRepository.save(refund);
            }
        }
    }
}
