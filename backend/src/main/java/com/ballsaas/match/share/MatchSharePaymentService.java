package com.ballsaas.match.share;

import com.ballsaas.common.BusinessException;
import com.ballsaas.match.MatchRoom;
import com.ballsaas.match.MatchRoomRepository;
import com.ballsaas.payment.BusinessType;
import com.ballsaas.payment.PaymentOrder;
import com.ballsaas.payment.PaymentOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchSharePaymentService {

    private final MatchSharePaymentRepository sharePaymentRepository;
    private final MatchRoomRepository matchRoomRepository;
    private final PaymentOrderRepository paymentOrderRepository;

    public MatchSharePaymentService(
            MatchSharePaymentRepository sharePaymentRepository,
            MatchRoomRepository matchRoomRepository,
            PaymentOrderRepository paymentOrderRepository) {
        this.sharePaymentRepository = sharePaymentRepository;
        this.matchRoomRepository = matchRoomRepository;
        this.paymentOrderRepository = paymentOrderRepository;
    }

    @Transactional
    public MatchSharePayment create(CreateSharePaymentRequest request) {
        MatchRoom room = matchRoomRepository.findById(request.matchRoomId())
                .orElseThrow(() -> new BusinessException("约战不存在"));
        if (request.amountCent() <= 0) {
            throw new BusinessException("分摊金额必须大于 0");
        }
        sharePaymentRepository.findByMatchRoomIdAndUserId(request.matchRoomId(), request.userId()).ifPresent(existing -> {
            throw new BusinessException("该用户已创建约战分摊支付");
        });
        MatchSharePayment sharePayment = sharePaymentRepository.save(new MatchSharePayment(request.matchRoomId(), request.userId(), request.amountCent()));
        PaymentOrder paymentOrder = paymentOrderRepository.save(new PaymentOrder(
                "MS" + System.currentTimeMillis(),
                BusinessType.MATCH_SHARE,
                sharePayment.getId(),
                room.getVenueId(),
                request.userId(),
                request.amountCent()
        ));
        sharePayment.attachPaymentOrder(paymentOrder.getId(), paymentOrder.getPaymentNo());
        return sharePaymentRepository.save(sharePayment);
    }
}
