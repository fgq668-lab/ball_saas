package com.ballsaas.refund;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefundOrderRepository extends JpaRepository<RefundOrder, Long> {

    List<RefundOrder> findByPaymentOrderId(Long paymentOrderId);

    Optional<RefundOrder> findByRefundNo(String refundNo);

    List<RefundOrder> findByVenueIdOrderByCreatedAtDesc(Long venueId);

    long countByStatus(RefundStatus status);

    long countByVenueIdAndStatus(Long venueId, RefundStatus status);

    @Query("""
            select coalesce(sum(r.amountCent), 0)
            from RefundOrder r
            where r.status = :status
              and r.createdAt >= :startAt
              and r.createdAt < :endAt
            """)
    int sumAmountCentByStatusAndCreatedAtBetween(
            @Param("status") RefundStatus status,
            @Param("startAt") OffsetDateTime startAt,
            @Param("endAt") OffsetDateTime endAt);

    @Query("""
            select coalesce(sum(r.amountCent), 0)
            from RefundOrder r
            where r.venueId = :venueId
              and r.status = :status
              and r.createdAt >= :startAt
              and r.createdAt < :endAt
            """)
    int sumAmountCentByVenueIdAndStatusAndCreatedAtBetween(
            @Param("venueId") Long venueId,
            @Param("status") RefundStatus status,
            @Param("startAt") OffsetDateTime startAt,
            @Param("endAt") OffsetDateTime endAt);
}
