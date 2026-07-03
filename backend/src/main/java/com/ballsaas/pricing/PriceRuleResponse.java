package com.ballsaas.pricing;

public record PriceRuleResponse(Long id, int priceCent) {

    static PriceRuleResponse from(CourtPriceRule rule) {
        return new PriceRuleResponse(rule.getId(), rule.getPriceCent());
    }
}

