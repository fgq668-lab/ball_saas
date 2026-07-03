package com.ballsaas.pricing;

import com.ballsaas.audit.AuditService;
import com.ballsaas.common.ApiResponse;
import com.ballsaas.tenant.RequestVenueContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/venue/price-rules")
public class PriceRuleController {

    private final CourtPriceRuleRepository priceRuleRepository;
    private final RequestVenueContext venueContext;
    private final AuditService auditService;

    public PriceRuleController(CourtPriceRuleRepository priceRuleRepository, RequestVenueContext venueContext, AuditService auditService) {
        this.priceRuleRepository = priceRuleRepository;
        this.venueContext = venueContext;
        this.auditService = auditService;
    }

    @PostMapping
    public ApiResponse<PriceRuleResponse> create(@Valid @RequestBody CreatePriceRuleRequest request, HttpServletRequest servletRequest) {
        venueContext.assertVenue(request.venueId(), servletRequest);
        CourtPriceRule rule = new CourtPriceRule(
                request.venueId(),
                request.courtId(),
                request.dayType(),
                request.startTime(),
                request.endTime(),
                request.priceCent(),
                request.priority()
        );
        CourtPriceRule saved = priceRuleRepository.save(rule);
        auditService.record("PRICE_RULE_CREATE", "COURT_PRICE_RULE", saved.getId(), request.venueId(), servletRequest, "保存价格规则: courtId=" + request.courtId());
        return ApiResponse.ok(PriceRuleResponse.from(saved));
    }
}
