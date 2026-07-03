package com.ballsaas.booking;

import com.ballsaas.common.BusinessException;
import com.ballsaas.pricing.PriceService;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

    private final BookingOrderRepository bookingOrderRepository;
    private final SlotService slotService;
    private final PriceService priceService;
    private final StringRedisTemplate redisTemplate;
    private final String redisPrefix;
    private final long lockMinutes;

    public BookingService(
            BookingOrderRepository bookingOrderRepository,
            SlotService slotService,
            PriceService priceService,
            StringRedisTemplate redisTemplate,
            @Value("${ballsaas.redis-prefix:ballsaas}") String redisPrefix,
            @Value("${ballsaas.booking-lock-minutes:10}") long lockMinutes) {
        this.bookingOrderRepository = bookingOrderRepository;
        this.slotService = slotService;
        this.priceService = priceService;
        this.redisTemplate = redisTemplate;
        this.redisPrefix = redisPrefix;
        this.lockMinutes = lockMinutes;
    }

    @Transactional
    public BookingOrder create(CreateBookingRequest request) {
        validateTime(request.startAt(), request.endAt());
        slotService.assertAvailable(request.venueId(), request.courtId(), request.startAt(), request.endAt());
        String lockKey = lockKey(request.venueId(), request.courtId(), request.startAt(), request.endAt());
        String lockToken = UUID.randomUUID().toString();
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, lockToken, Duration.ofMinutes(lockMinutes));
        if (!Boolean.TRUE.equals(locked)) {
            throw new BusinessException("所选时间段正在支付锁定中");
        }
        int amountCent = priceService.calculate(request.venueId(), request.courtId(), request.startAt(), request.endAt());
        BookingOrder order = new BookingOrder(
                "B" + System.currentTimeMillis(),
                request.venueId(),
                request.courtId(),
                request.userId(),
                request.startAt(),
                request.endAt(),
                amountCent,
                lockToken
        );
        return bookingOrderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<BookingOrder> listByUser(Long userId) {
        return bookingOrderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public BookingOrder detailForUser(Long bookingId, Long userId) {
        BookingOrder order = bookingOrderRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("预约订单不存在"));
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("不能访问他人订单");
        }
        return order;
    }

    @Transactional
    public BookingOrder confirmPaid(Long bookingId, OffsetDateTime paidAt) {
        BookingOrder order = bookingOrderRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("预约订单不存在"));
        if (order.getStatus() == BookingStatus.RESERVED) {
            return order;
        }
        if (order.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BusinessException("当前订单状态不能确认支付");
        }
        slotService.assertAvailable(order.getVenueId(), order.getCourtId(), order.getStartAt(), order.getEndAt());
        try {
            slotService.createSlots(order);
            order.markReserved(paidAt);
            return bookingOrderRepository.save(order);
        } catch (DataIntegrityViolationException ex) {
            order.markManualReview();
            bookingOrderRepository.save(order);
            throw new BusinessException("预约时间槽写入失败，订单进入人工处理");
        }
    }

    @Transactional
    public BookingOrder cancel(Long bookingId) {
        BookingOrder order = bookingOrderRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("预约订单不存在"));
        return cancelOrder(order);
    }

    @Transactional
    public BookingOrder cancelForUser(Long bookingId, Long userId) {
        BookingOrder order = detailForUser(bookingId, userId);
        return cancelOrder(order);
    }

    private BookingOrder cancelOrder(BookingOrder order) {
        if (order.getStatus() == BookingStatus.CHECKED_IN || order.getStatus() == BookingStatus.COMPLETED) {
            throw new BusinessException("已核销或已完成订单不能普通取消");
        }
        if (order.getStatus() == BookingStatus.RESERVED) {
            throw new BusinessException("已支付订单请走退款流程");
        }
        if (order.getStatus() == BookingStatus.CANCELLED || order.getStatus() == BookingStatus.PAYMENT_TIMEOUT) {
            return order;
        }
        order.cancel();
        return bookingOrderRepository.save(order);
    }

    @Transactional
    public int closeTimeoutOrders(OffsetDateTime now) {
        OffsetDateTime createdBefore = now.minusMinutes(lockMinutes);
        List<BookingOrder> orders = bookingOrderRepository.findByStatusAndCreatedAtBefore(BookingStatus.PENDING_PAYMENT, createdBefore);
        int closed = 0;
        for (BookingOrder order : orders) {
            order.markPaymentTimeout();
            bookingOrderRepository.save(order);
            closed++;
        }
        return closed;
    }

    private void validateTime(OffsetDateTime startAt, OffsetDateTime endAt) {
        if (!startAt.isBefore(endAt)) {
            throw new BusinessException("开始时间必须早于结束时间");
        }
        long minutes = Duration.between(startAt, endAt).toMinutes();
        if (minutes % 30 != 0) {
            throw new BusinessException("预约时间必须按 30 分钟粒度选择");
        }
    }

    private String lockKey(Long venueId, Long courtId, OffsetDateTime startAt, OffsetDateTime endAt) {
        return redisPrefix + ":court-lock:" + venueId + ":" + courtId + ":" + startAt + ":" + endAt;
    }
}
