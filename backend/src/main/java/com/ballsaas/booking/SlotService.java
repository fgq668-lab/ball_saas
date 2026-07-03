package com.ballsaas.booking;

import com.ballsaas.common.BusinessException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SlotService {

    private static final int SLOT_MINUTES = 30;

    private final CourtTimeSlotRepository slotRepository;

    public SlotService(CourtTimeSlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    public void assertAvailable(Long venueId, Long courtId, OffsetDateTime startAt, OffsetDateTime endAt) {
        List<CourtTimeSlot> existing = slotRepository
                .findByVenueIdAndCourtIdAndSlotStartAtGreaterThanEqualAndSlotStartAtLessThan(venueId, courtId, startAt, endAt);
        if (!existing.isEmpty()) {
            throw new BusinessException("所选时间段已被预约");
        }
    }

    public void createSlots(BookingOrder order) {
        slotRepository.saveAll(buildSlots(order));
    }

    private List<CourtTimeSlot> buildSlots(BookingOrder order) {
        long minutes = ChronoUnit.MINUTES.between(order.getStartAt(), order.getEndAt());
        if (minutes <= 0 || minutes % SLOT_MINUTES != 0) {
            throw new BusinessException("预约时间必须按 30 分钟粒度选择");
        }
        List<CourtTimeSlot> slots = new ArrayList<>();
        OffsetDateTime cursor = order.getStartAt();
        while (cursor.isBefore(order.getEndAt())) {
            OffsetDateTime slotEnd = cursor.plusMinutes(SLOT_MINUTES);
            slots.add(new CourtTimeSlot(order.getVenueId(), order.getCourtId(), cursor, slotEnd, order.getId()));
            cursor = slotEnd;
        }
        return slots;
    }
}

