package com.ballsaas.booking;

import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourtTimeSlotRepository extends JpaRepository<CourtTimeSlot, Long> {

    List<CourtTimeSlot> findByVenueIdAndCourtIdAndSlotStartAtGreaterThanEqualAndSlotStartAtLessThan(
            Long venueId, Long courtId, OffsetDateTime startAt, OffsetDateTime endAt);

    void deleteByBookingOrderId(Long bookingOrderId);
}