package com.ballsaas.court;

import com.ballsaas.audit.AuditService;
import com.ballsaas.common.ApiResponse;
import com.ballsaas.tenant.RequestVenueContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/venue/courts")
public class CourtController {

    private final CourtRepository courtRepository;
    private final RequestVenueContext venueContext;
    private final AuditService auditService;

    public CourtController(CourtRepository courtRepository, RequestVenueContext venueContext, AuditService auditService) {
        this.courtRepository = courtRepository;
        this.venueContext = venueContext;
        this.auditService = auditService;
    }

    @GetMapping
    public ApiResponse<List<CourtResponse>> list(HttpServletRequest request) {
        Long scopedVenueId = venueContext.requireVenueId(request);
        return ApiResponse.ok(courtRepository.findByVenueIdAndDeletedFalse(scopedVenueId).stream().map(CourtResponse::from).toList());
    }

    @PostMapping
    public ApiResponse<CourtResponse> create(@Valid @RequestBody CreateCourtRequest requestBody, HttpServletRequest request) {
        venueContext.assertVenue(requestBody.venueId(), request);
        Court court = new Court(requestBody.venueId(), requestBody.name(), requestBody.sportType(), requestBody.indoor());
        Court saved = courtRepository.save(court);
        auditService.record("COURT_CREATE", "COURT", saved.getId(), saved.getVenueId(), request, "新增场地: " + saved.getName());
        return ApiResponse.ok(CourtResponse.from(saved));
    }

    @PostMapping("/{courtId}/disable")
    public ApiResponse<CourtResponse> disable(@PathVariable Long courtId, HttpServletRequest request) {
        Court court = courtRepository.findById(courtId).orElseThrow();
        venueContext.assertVenue(court.getVenueId(), request);
        court.disable();
        Court saved = courtRepository.save(court);
        auditService.record("COURT_DISABLE", "COURT", saved.getId(), saved.getVenueId(), request, "停用场地: " + saved.getName());
        return ApiResponse.ok(CourtResponse.from(saved));
    }
}
