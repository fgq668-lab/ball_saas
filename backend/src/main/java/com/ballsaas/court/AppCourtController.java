package com.ballsaas.court;

import com.ballsaas.common.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/venues/{venueId}/courts")
public class AppCourtController {

    private final CourtRepository courtRepository;

    public AppCourtController(CourtRepository courtRepository) {
        this.courtRepository = courtRepository;
    }

    @GetMapping("/availability")
    public ApiResponse<List<CourtResponse>> availability(@PathVariable Long venueId) {
        return ApiResponse.ok(courtRepository.findByVenueIdAndDeletedFalse(venueId).stream().map(CourtResponse::from).toList());
    }
}

