package com.ballsaas.pricing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.ballsaas.common.BusinessException;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PriceServiceTest {

    @Mock
    private CourtPriceRuleRepository priceRuleRepository;

    @InjectMocks
    private PriceService priceService;

    @Test
    void calculatesByThirtyMinuteSlots() {
        CourtPriceRule rule = new CourtPriceRule(
                1L,
                2L,
                DayType.WORKDAY,
                LocalTime.of(8, 0),
                LocalTime.of(22, 0),
                3000,
                1
        );
        OffsetDateTime startAt = OffsetDateTime.parse("2026-07-01T10:00:00+08:00");
        OffsetDateTime endAt = OffsetDateTime.parse("2026-07-01T11:30:00+08:00");
        when(priceRuleRepository.findByVenueIdAndCourtIdAndEnabledTrueOrderByPriorityDesc(1L, 2L))
                .thenReturn(List.of(rule));

        int amountCent = priceService.calculate(1L, 2L, startAt, endAt);

        assertThat(amountCent).isEqualTo(9000);
    }

    @Test
    void rejectsNonThirtyMinuteDuration() {
        OffsetDateTime startAt = OffsetDateTime.parse("2026-07-01T10:00:00+08:00");
        OffsetDateTime endAt = OffsetDateTime.parse("2026-07-01T10:20:00+08:00");

        assertThatThrownBy(() -> priceService.calculate(1L, 2L, startAt, endAt))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("30 分钟");
    }

    @Test
    void rejectsMissingPriceRule() {
        OffsetDateTime startAt = OffsetDateTime.parse("2026-07-01T10:00:00+08:00");
        OffsetDateTime endAt = OffsetDateTime.parse("2026-07-01T10:30:00+08:00");
        when(priceRuleRepository.findByVenueIdAndCourtIdAndEnabledTrueOrderByPriorityDesc(1L, 2L))
                .thenReturn(List.of());

        assertThatThrownBy(() -> priceService.calculate(1L, 2L, startAt, endAt))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("未配置有效价格");
    }
}
