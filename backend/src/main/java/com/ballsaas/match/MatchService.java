package com.ballsaas.match;

import com.ballsaas.booking.BookingOrder;
import com.ballsaas.booking.BookingOrderRepository;
import com.ballsaas.booking.BookingStatus;
import com.ballsaas.common.BusinessException;
import com.ballsaas.match.share.MatchSharePayment;
import com.ballsaas.match.share.MatchSharePaymentRepository;
import com.ballsaas.match.share.MatchSharePaymentStatus;
import com.ballsaas.match.share.MatchShareRefund;
import com.ballsaas.match.share.MatchShareRefundRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchService {

    private final MatchRoomRepository matchRoomRepository;
    private final MatchPlayerRepository matchPlayerRepository;
    private final BookingOrderRepository bookingOrderRepository;
    private final MatchSharePaymentRepository sharePaymentRepository;
    private final MatchShareRefundRepository shareRefundRepository;

    public MatchService(
            MatchRoomRepository matchRoomRepository,
            MatchPlayerRepository matchPlayerRepository,
            BookingOrderRepository bookingOrderRepository,
            MatchSharePaymentRepository sharePaymentRepository,
            MatchShareRefundRepository shareRefundRepository) {
        this.matchRoomRepository = matchRoomRepository;
        this.matchPlayerRepository = matchPlayerRepository;
        this.bookingOrderRepository = bookingOrderRepository;
        this.sharePaymentRepository = sharePaymentRepository;
        this.shareRefundRepository = shareRefundRepository;
    }

    @Transactional(readOnly = true)
    public List<MatchRoom> list(Long venueId, Long creatorUserId) {
        if (venueId != null) {
            return matchRoomRepository.findByVenueIdOrderByStartAtDesc(venueId);
        }
        if (creatorUserId != null) {
            return matchRoomRepository.findByCreatorUserIdOrderByStartAtDesc(creatorUserId);
        }
        return matchRoomRepository.findAll();
    }

    @Transactional(readOnly = true)
    public MatchRoom detail(Long matchId) {
        return matchRoomRepository.findById(matchId)
                .orElseThrow(() -> new BusinessException("约战不存在"));
    }

    @Transactional(readOnly = true)
    public List<MatchPlayer> listPlayers(Long matchId) {
        return matchPlayerRepository.findByMatchRoomId(matchId);
    }

    @Transactional
    public MatchRoom create(CreateMatchRequest request) {
        BookingOrder booking = bookingOrderRepository.findById(request.bookingOrderId())
                .orElseThrow(() -> new BusinessException("预约订单不存在"));
        if (booking.getStatus() != BookingStatus.RESERVED) {
            throw new BusinessException("只有已预约订单可以创建约战");
        }
        MatchRoom room = new MatchRoom(
                "M" + System.currentTimeMillis(),
                booking.getId(),
                booking.getVenueId(),
                booking.getCourtId(),
                request.creatorUserId(),
                booking.getStartAt(),
                booking.getEndAt(),
                request.minPlayers(),
                request.maxPlayers(),
                booking.getPayableCent()
        );
        MatchRoom saved = matchRoomRepository.save(room);
        matchPlayerRepository.save(new MatchPlayer(saved.getId(), request.creatorUserId()));
        return saved;
    }

    @Transactional
    public MatchRoom join(Long matchId, JoinMatchRequest request) {
        MatchRoom room = matchRoomRepository.findById(matchId)
                .orElseThrow(() -> new BusinessException("约战不存在"));
        if (!room.canChangePlayers()) {
            throw new BusinessException("当前约战状态不允许加入");
        }
        if (room.getCurrentPlayers() >= room.getMaxPlayers()) {
            throw new BusinessException("约战人数已满");
        }
        matchPlayerRepository.findByMatchRoomIdAndUserId(matchId, request.userId()).ifPresent(player -> {
            throw new BusinessException("用户已加入该约战");
        });
        matchPlayerRepository.save(new MatchPlayer(matchId, request.userId()));
        room.playerJoined();
        return matchRoomRepository.save(room);
    }

    @Transactional
    public MatchRoom leave(Long matchId, JoinMatchRequest request) {
        MatchRoom room = matchRoomRepository.findById(matchId)
                .orElseThrow(() -> new BusinessException("约战不存在"));
        if (!room.canChangePlayers()) {
            throw new BusinessException("当前约战状态不允许退出");
        }
        if (room.getCreatorUserId().equals(request.userId())) {
            throw new BusinessException("创建者请取消约战");
        }
        MatchPlayer player = matchPlayerRepository.findByMatchRoomIdAndUserId(matchId, request.userId())
                .orElseThrow(() -> new BusinessException("用户未加入该约战"));
        if (player.getStatus() == MatchPlayerStatus.LEFT) {
            return room;
        }
        player.markLeft();
        matchPlayerRepository.save(player);
        refundPaidShareIfNeeded(matchId, request.userId());
        room.playerLeft();
        return matchRoomRepository.save(room);
    }

    @Transactional
    public MatchRoom cancelNotFormed(Long matchId) {
        MatchRoom room = matchRoomRepository.findById(matchId)
                .orElseThrow(() -> new BusinessException("约战不存在"));
        return cancelNotFormed(room);
    }

    @Transactional
    public MatchRoom cancelNotFormedForCreator(Long matchId, Long userId) {
        MatchRoom room = matchRoomRepository.findById(matchId)
                .orElseThrow(() -> new BusinessException("约战不存在"));
        if (!room.getCreatorUserId().equals(userId)) {
            throw new BusinessException("只有创建者可以取消未成局约战");
        }
        return cancelNotFormed(room);
    }

    private MatchRoom cancelNotFormed(MatchRoom room) {
        if (room.getStatus() == MatchStatus.FORMED) {
            throw new BusinessException("已成局约战不能按未成局取消");
        }
        if (room.getStatus() == MatchStatus.NOT_FORMED_CANCELLED) {
            return room;
        }
        room.cancelNotFormed();
        sharePaymentRepository.findByMatchRoomId(room.getId()).stream()
                .filter(payment -> payment.getStatus() == MatchSharePaymentStatus.PAID)
                .forEach(this::refundPaidShare);
        return matchRoomRepository.save(room);
    }

    private void refundPaidShareIfNeeded(Long matchId, Long userId) {
        sharePaymentRepository.findByMatchRoomIdAndUserId(matchId, userId)
                .filter(payment -> payment.getStatus() == MatchSharePaymentStatus.PAID)
                .ifPresent(this::refundPaidShare);
    }

    private void refundPaidShare(MatchSharePayment payment) {
        payment.markRefunded();
        sharePaymentRepository.save(payment);
        MatchShareRefund refund = new MatchShareRefund(payment.getMatchRoomId(), payment.getUserId(), payment.getAmountCent());
        refund.markRefunded();
        shareRefundRepository.save(refund);
    }
}

