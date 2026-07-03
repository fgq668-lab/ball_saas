package com.ballsaas.booking;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingOrderRepository extends JpaRepository<BookingOrder, Long> {

    Optional<BookingOrder> findByOrderNo(String orderNo);

    List<BookingOrder> findByVenueIdOrderByCreatedAtDesc(Long venueId);

    List<BookingOrder> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<BookingOrder> findByStatusAndCreatedAtBefore(BookingStatus status, OffsetDateTime createdBefore);

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(OffsetDateTime startAt, OffsetDateTime endAt);

    long countByVenueIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(Long venueId, OffsetDateTime startAt, OffsetDateTime endAt);

    @Query("""
            select coalesce(sum(b.payableCent), 0)
            from BookingOrder b
            where b.status in :statuses
              and b.createdAt >= :startAt
              and b.createdAt < :endAt
            """)
    int sumPayableCentByStatusesAndCreatedAtBetween(
            @Param("statuses") List<BookingStatus> statuses,
            @Param("startAt") OffsetDateTime startAt,
            @Param("endAt") OffsetDateTime endAt);

    @Query("""
            select coalesce(sum(b.payableCent), 0)
            from BookingOrder b
            where b.venueId = :venueId
              and b.status in :statuses
              and b.createdAt >= :startAt
              and b.createdAt < :endAt
            """)
    int sumPayableCentByVenueIdAndStatusesAndCreatedAtBetween(
            @Param("venueId") Long venueId,
            @Param("statuses") List<BookingStatus> statuses,
            @Param("startAt") OffsetDateTime startAt,
            @Param("endAt") OffsetDateTime endAt);
}