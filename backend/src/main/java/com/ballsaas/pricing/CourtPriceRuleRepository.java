package com.ballsaas.pricing;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourtPriceRuleRepository extends JpaRepository<CourtPriceRule, Long> {

    List<CourtPriceRule> findByVenueIdAndCourtIdAndEnabledTrueOrderByPriorityDesc(Long venueId, Long courtId);
}

