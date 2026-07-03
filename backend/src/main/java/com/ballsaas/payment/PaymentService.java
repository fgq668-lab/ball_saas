package com.ballsaas.payment;

import com.ballsaas.booking.BookingOrder;
import com.ballsaas.booking.BookingOrderRepository;
import com.ballsaas.booking.BookingService;
import com.ballsaas.common.BusinessException;
import com.ballsaas.match.MatchPlayer;
import com.ballsaas.match.MatchPlayerRepository;
import com.ballsaas.match.MatchRoom;
import com.ballsaas.match.MatchRoomRepository;
import com.ballsaas.match.share.MatchSharePayment;
import com.ballsaas.match.share.MatchSharePaymentRepository;
import com.ballsaas.match.share.MatchSharePaymentStatus;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentNotifyLogRepository notifyLogRepository;
    private final BookingOrderRepository bookingOrderRepository;
    private final BookingService bookingService;
    private final MatchSharePaymentRepository sharePaymentRepository;
    private final MatchRoomRepository matchRoomRepository;
    private final MatchPlayerRepository matchPlayerRepository;

    public PaymentService(
            PaymentOrderRepository paymentOrderRepository,
            PaymentNotifyLogRepository notifyLogRepository,
            BookingOrderRepository bookingOrderRepository,
            BookingService bookingService,
            MatchSharePaymentRepository sharePaymentRepository,
            MatchRoomRepository matchRoomRepository,
            MatchPlayerRepository matchPlayerRepository) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.notifyLogRepository = notifyLogRepository;
        this.bookingOrderRepository = bookingOrderRepository;
        this.bookingService = bookingService;
        this.sharePaymentRepository = sharePaymentRepository;
        this.matchRoomRepository = matchRoomRepository;
        this.matchPlayerRepository = matchPlayerRepository;
    }

    @Transactional
    public PaymentOrder prepayBooking(Long bookingId) {
        BookingOrder booking = bookingOrderRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("预约订单不存在"));
        return createBookingPayment(booking);
    }

    @Transactional
    public PaymentOrder prepayBookingForUser(Long bookingId, Long userId) {
        BookingOrder booking = bookingOrderRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("预约订单不存在"));
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException("不能支付他人预约订单");
        }
        return createBookingPayment(booking);
    }

    private PaymentOrder createBookingPayment(BookingOrder booking) {
        PaymentOrder paymentOrder = new PaymentOrder(
                "P" + System.currentTimeMillis(),
                BusinessType.BOOKING,
                booking.getId(),
                booking.getVenueId(),
                booking.getUserId(),
                booking.getPayableCent()
        );
        return paymentOrderRepository.save(paymentOrder);
    }

    @Transactional
    public PaymentOrder mockPaySuccess(String paymentNo) {
        PaymentOrder paymentOrder = paymentOrderRepository.findByPaymentNo(paymentNo)
                .orElseThrow(() -> new BusinessException("支付单不存在"));
        boolean alreadyPaid = paymentOrder.getStatus() == PaymentStatus.PAID;
        notifyLogRepository.save(new PaymentNotifyLog(
                "MOCK",
                "PAYMENT_SUCCESS",
                paymentNo,
                "mock payment success: " + paymentNo,
                "{}",
                alreadyPaid ? "DUPLICATE" : "SUCCESS"
        ));
        if (alreadyPaid) {
            return paymentOrder;
        }
        OffsetDateTime paidAt = OffsetDateTime.now();
        if (paymentOrder.getBusinessType() == BusinessType.BOOKING) {
            bookingService.confirmPaid(paymentOrder.getBusinessId(), paidAt);
        }
        paymentOrder.markPaid("MOCK-" + paymentNo, paidAt);
        paymentOrderRepository.save(paymentOrder);
        if (paymentOrder.getBusinessType() == BusinessType.MATCH_SHARE) {
            markSharePaid(paymentOrder);
        }
        return paymentOrder;
    }

    @Transactional
    public void recordMockWechatNotify(String rawBody) {
        String paymentNo = extractPaymentNo(rawBody);
        notifyLogRepository.save(new PaymentNotifyLog(
                "WECHAT",
                "PAYMENT_NOTIFY",
                paymentNo,
                rawBody,
                "{}",
                "ACCEPTED"
        ));
    }

    private String extractPaymentNo(String rawBody) {
        if (rawBody == null || rawBody.isBlank()) {
            return null;
        }
        int marker = rawBody.indexOf("P");
        if (marker < 0) {
            return null;
        }
        int end = marker + 1;
        while (end < rawBody.length() && Character.isDigit(rawBody.charAt(end))) {
            end++;
        }
        return rawBody.substring(marker, end);
    }

    private void markSharePaid(PaymentOrder paymentOrder) {
        MatchSharePayment sharePayment = sharePaymentRepository.findByPaymentOrderId(paymentOrder.getId())
                .orElseThrow(() -> new BusinessException("约战分摊支付不存在"));
        if (sharePayment.getStatus() != MatchSharePaymentStatus.PAID) {
            sharePayment.markPaid();
            sharePaymentRepository.save(sharePayment);
        }
        MatchPlayer player = matchPlayerRepository.findByMatchRoomIdAndUserId(sharePayment.getMatchRoomId(), sharePayment.getUserId())
                .orElseThrow(() -> new BusinessException("约战成员不存在"));
        player.markPaid();
        matchPlayerRepository.save(player);
        MatchRoom room = matchRoomRepository.findById(sharePayment.getMatchRoomId())
                .orElseThrow(() -> new BusinessException("约战不存在"));
        long paidCountIncludingCreator = sharePaymentRepository.countByMatchRoomIdAndStatus(room.getId(), MatchSharePaymentStatus.PAID) + 1;
        if (paidCountIncludingCreator >= room.getMinPlayers()) {
            room.form();
            matchRoomRepository.save(room);
        }
    }
}


