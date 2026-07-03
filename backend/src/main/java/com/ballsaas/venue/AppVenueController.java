package com.ballsaas.venue;

import com.ballsaas.common.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/venues")
public class AppVenueController {

    private final VenueRepository venueRepository;

    public AppVenueController(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    @GetMapping
    public ApiResponse<List<VenueResponse>> list() {
        return ApiResponse.ok(venueRepository.findAll().stream().map(VenueResponse::from).toList());
    }

    @GetMapping("/{venueId}")
    public ApiResponse<VenueResponse> detail(@PathVariable Long venueId) {
        return ApiResponse.ok(VenueResponse.from(venueRepository.findById(venueId).orElseThrow()));
    }
}

