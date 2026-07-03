package com.ballsaas.venue;

import com.ballsaas.audit.AuditService;
import com.ballsaas.common.ApiResponse;
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
@RequestMapping("/api/admin/venues")
public class VenueController {

    private final VenueRepository venueRepository;
    private final AuditService auditService;

    public VenueController(VenueRepository venueRepository, AuditService auditService) {
        this.venueRepository = venueRepository;
        this.auditService = auditService;
    }

    @GetMapping
    public ApiResponse<List<VenueResponse>> list() {
        return ApiResponse.ok(venueRepository.findAll().stream().map(VenueResponse::from).toList());
    }

    @PostMapping
    public ApiResponse<VenueResponse> create(@Valid @RequestBody CreateVenueRequest request, HttpServletRequest servletRequest) {
        Venue venue = new Venue(request.name(), request.sportTypes(), request.address(), request.contactName(), request.contactPhone());
        Venue saved = venueRepository.save(venue);
        auditService.record("VENUE_CREATE", "VENUE", saved.getId(), saved.getId(), servletRequest, "提交场馆入驻: " + saved.getName());
        return ApiResponse.ok(VenueResponse.from(saved));
    }

    @PostMapping("/{venueId}/approve")
    public ApiResponse<VenueResponse> approve(@PathVariable Long venueId, HttpServletRequest request) {
        Venue venue = venueRepository.findById(venueId).orElseThrow();
        venue.approve();
        Venue saved = venueRepository.save(venue);
        auditService.record("VENUE_APPROVE", "VENUE", saved.getId(), saved.getId(), request, "审核通过场馆: " + saved.getName());
        return ApiResponse.ok(VenueResponse.from(saved));
    }
}
