package com.ballsaas.pricing;

import com.ballsaas.common.BusinessException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PriceService {

    private final CourtPriceRuleRepository priceRuleRepository;

    public PriceService(CourtPriceRuleRepository priceRuleRepository) {
        this.priceRuleRepository = priceRuleRepository;
    }

    public int calculate(Long venueId, Long courtId, OffsetDateTime startAt, OffsetDateTime endAt) {
        long minutes = ChronoUnit.MINUTES.between(startAt, endAt);
        if (minutes <= 0 || minutes % 30 != 0) {
            throw new BusinessException("预约时间必须按 30 分钟粒度选择");
        }
        List<CourtPriceRule> rules = priceRuleRepository.findByVenueIdAndCourtIdAndEnabledTrueOrderByPriorityDesc(venueId, courtId);
        CourtPriceRule rule = rules.stream().findFirst()
                .orElseThrow(() -> new BusinessException("场地未配置有效价格"));
        return (int) ((minutes / 30) * rule.getPriceCent());
    }
}

