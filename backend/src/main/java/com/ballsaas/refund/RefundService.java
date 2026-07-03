package com.ballsaas.refund;

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
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefundService {

    private final RefundOrderRepository refundOrderRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final BookingOrderRepository bookingOrderRepository;
    private final CourtTimeSlotRepository slotRepository;
    private final PaymentNotifyLogRepository notifyLogRepository;

    public RefundService(
            RefundOrderRepository refundOrderRepository,
            PaymentOrderRepository paymentOrderRepository,
            BookingOrderRepository bookingOrderRepository,
            CourtTimeSlotRepository slotRepository,
            PaymentNotifyLogRepository notifyLogRepository) {
        this.refundOrderRepository = refundOrderRepository;
        this.paymentOrderRepository = paymentOrderRepository;
        this.bookingOrderRepository = bookingOrderRepository;
        this.slotRepository = slotRepository;
        this.notifyLogRepository = notifyLogRepository;
    }

    @Transactional
    public RefundOrder create(CreateRefundRequest request) {
        PaymentOrder payment = paymentOrderRepository.findById(request.paymentOrderId())
                .orElseThrow(() -> new BusinessException("支付单不存在"));
        return createForPayment(request, payment);
    }

    @Transactional
    public RefundOrder createForUser(CreateRefundRequest request, Long userId) {
        PaymentOrder payment = paymentOrderRepository.findById(request.paymentOrderId())
                .orElseThrow(() -> new BusinessException("支付单不存在"));
        if (!payment.getUserId().equals(userId)) {
            throw new BusinessException("不能申请他人支付单退款");
        }
        return createForPayment(request, payment);
    }

    private RefundOrder createForPayment(CreateRefundRequest request, PaymentOrder payment) {
        if (payment.getStatus() != PaymentStatus.PAID && payment.getStatus() != PaymentStatus.PARTIALLY_REFUNDED) {
            throw new BusinessException("只有已支付订单可以退款");
        }
        if (!payment.getVenueId().equals(request.venueId())) {
            throw new BusinessException("退款场馆与支付单不匹配");
        }
        int refundedCent = refundOrderRepository.findByPaymentOrderId(payment.getId()).stream()
                .filter(this::isActiveRefund)
                .mapToInt(RefundOrder::getAmountCent)
                .sum();
        if (refundedCent + request.amountCent() > payment.getAmountCent()) {
            throw new BusinessException("退款金额超过可退金额");
        }
        RefundOrder order = new RefundOrder(
                "R" + System.currentTimeMillis(),
                request.paymentOrderId(),
                request.venueId(),
                request.amountCent(),
                request.reason()
        );
        RefundOrder saved = refundOrderRepository.save(order);
        saved.markRefunded("MOCK-REFUND-" + saved.getId());
        updateBusinessAfterRefund(payment, refundedCent + request.amountCent());
        RefundOrder refunded = refundOrderRepository.save(saved);
        saveNotifyLog("MOCK", "REFUND_SUCCESS", refunded.getRefundNo(), "mock refund success: " + refunded.getRefundNo(), "SUCCESS");
        return refunded;
    }
    @Transactional
    public RefundOrder mockRefundSuccess(String refundNo) {
        return handleRefundSuccess(refundNo, "MOCK", "REFUND_SUCCESS", "mock refund success: " + refundNo);
    }

    @Transactional
    public void recordMockWechatRefundNotify(String rawBody) {
        String refundNo = extractRefundNo(rawBody);
        if (refundNo == null) {
            saveNotifyLog("WECHAT", "REFUND_NOTIFY", null, rawBody, "ACCEPTED");
            return;
        }
        handleRefundSuccess(refundNo, "WECHAT", "REFUND_NOTIFY", rawBody);
    }

    private RefundOrder handleRefundSuccess(String refundNo, String channel, String notifyType, String rawBody) {
        RefundOrder refund = refundOrderRepository.findByRefundNo(refundNo)
                .orElseThrow(() -> new BusinessException("退款单不存在"));
        boolean alreadyRefunded = refund.getStatus() == RefundStatus.REFUNDED;
        saveNotifyLog(channel, notifyType, refundNo, rawBody, alreadyRefunded ? "DUPLICATE" : "SUCCESS");
        if (alreadyRefunded) {
            return refund;
        }
        PaymentOrder payment = paymentOrderRepository.findById(refund.getPaymentOrderId())
                .orElseThrow(() -> new BusinessException("支付单不存在"));
        int totalRefundedCent = totalRefundedCentIncluding(payment, refund);
        refund.markRefunded(channel + "-REFUND-" + refund.getId());
        updateBusinessAfterRefund(payment, totalRefundedCent);
        return refundOrderRepository.save(refund);
    }

    private int totalRefundedCentIncluding(PaymentOrder payment, RefundOrder currentRefund) {
        int existingRefundedCent = refundOrderRepository.findByPaymentOrderId(payment.getId()).stream()
                .filter(order -> !Objects.equals(order.getId(), currentRefund.getId()))
                .filter(this::isActiveRefund)
                .mapToInt(RefundOrder::getAmountCent)
                .sum();
        return existingRefundedCent + currentRefund.getAmountCent();
    }

    private boolean isActiveRefund(RefundOrder order) {
        return order.getStatus() == RefundStatus.REFUNDED
                || order.getStatus() == RefundStatus.REQUESTED
                || order.getStatus() == RefundStatus.PROCESSING;
    }

    private void saveNotifyLog(String channel, String notifyType, String refundNo, String rawBody, String processStatus) {
        notifyLogRepository.save(new PaymentNotifyLog(
                channel,
                notifyType,
                refundNo,
                rawBody,
                "{}",
                processStatus
        ));
    }

    private String extractRefundNo(String rawBody) {
        if (rawBody == null || rawBody.isBlank()) {
            return null;
        }
        int marker = rawBody.indexOf("R");
        if (marker < 0) {
            return null;
        }
        int end = marker + 1;
        while (end < rawBody.length() && Character.isDigit(rawBody.charAt(end))) {
            end++;
        }
        return end > marker + 1 ? rawBody.substring(marker, end) : null;
    }

    private void updateBusinessAfterRefund(PaymentOrder payment, int totalRefundedCent) {
        boolean fullyRefunded = totalRefundedCent >= payment.getAmountCent();
        if (fullyRefunded) {
            payment.markRefunded();
        } else {
            payment.markPartiallyRefunded();
        }
        paymentOrderRepository.save(payment);

        if (payment.getBusinessType() == BusinessType.BOOKING) {
            BookingOrder booking = bookingOrderRepository.findById(payment.getBusinessId())
                    .orElseThrow(() -> new BusinessException("预约订单不存在"));
            if (fullyRefunded) {
                boolean releaseSlots = booking.getStartAt().isAfter(OffsetDateTime.now())
                        && booking.getStatus() != BookingStatus.CHECKED_IN
                        && booking.getStatus() != BookingStatus.COMPLETED;
                booking.markRefunded();
                if (releaseSlots) {
                    slotRepository.deleteByBookingOrderId(booking.getId());
                }
            } else {
                booking.markPartiallyRefunded();
            }
            bookingOrderRepository.save(booking);
        }
    }
}

