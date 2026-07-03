package com.ballsaas.report;

import com.ballsaas.common.ApiResponse;
import com.ballsaas.tenant.RequestVenueContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReportController {

    private final ReportService reportService;
    private final RequestVenueContext venueContext;

    public ReportController(ReportService reportService, RequestVenueContext venueContext) {
        this.reportService = reportService;
        this.venueContext = venueContext;
    }

    @GetMapping("/api/admin/dashboard")
    public ApiResponse<DashboardMetric> platformDashboard() {
        return ApiResponse.ok(reportService.platformDashboard());
    }

    @GetMapping("/api/venue/dashboard")
    public ApiResponse<DashboardMetric> venueDashboard(@RequestParam(required = false) Long venueId, HttpServletRequest request) {
        Long scopedVenueId = venueContext.requireVenueId(request);
        if (venueId != null && !venueId.equals(scopedVenueId)) {
            venueContext.assertVenue(venueId, request);
        }
        return ApiResponse.ok(reportService.venueDashboard(scopedVenueId));
    }

    @GetMapping("/api/admin/settlements")
    public ApiResponse<String> settlements() {
        return ApiResponse.ok("结算报表占位，后续接入真实分账/对账数据");
    }

    @GetMapping("/api/venue/reports/orders")
    public ApiResponse<String> venueOrderReport(@RequestParam(required = false) Long venueId, HttpServletRequest request) {
        Long scopedVenueId = venueContext.requireVenueId(request);
        if (venueId != null && !venueId.equals(scopedVenueId)) {
            venueContext.assertVenue(venueId, request);
        }
        return ApiResponse.ok("场馆订单报表占位：" + scopedVenueId);
    }
}