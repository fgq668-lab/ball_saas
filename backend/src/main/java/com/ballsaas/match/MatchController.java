package com.ballsaas.match;

import com.ballsaas.common.ApiResponse;
import com.ballsaas.security.RequestUserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/matches")
public class MatchController {

    private final MatchService matchService;
    private final RequestUserContext userContext;

    public MatchController(MatchService matchService, RequestUserContext userContext) {
        this.matchService = matchService;
        this.userContext = userContext;
    }

    @GetMapping
    public ApiResponse<List<MatchResponse>> list(@RequestParam(required = false) Long venueId, @RequestParam(required = false) Long creatorUserId, HttpServletRequest request) {
        Long scopedCreatorUserId = creatorUserId == null ? null : userContext.resolveUserId(request, creatorUserId);
        return ApiResponse.ok(matchService.list(venueId, scopedCreatorUserId).stream().map(MatchResponse::from).toList());
    }

    @GetMapping("/{matchId}")
    public ApiResponse<MatchResponse> detail(@PathVariable Long matchId) {
        return ApiResponse.ok(MatchResponse.from(matchService.detail(matchId)));
    }

    @GetMapping("/{matchId}/players")
    public ApiResponse<List<MatchPlayerResponse>> players(@PathVariable Long matchId) {
        return ApiResponse.ok(matchService.listPlayers(matchId).stream().map(MatchPlayerResponse::from).toList());
    }

    @PostMapping
    public ApiResponse<MatchResponse> create(@Valid @RequestBody CreateMatchRequest request, HttpServletRequest servletRequest) {
        Long creatorUserId = userContext.resolveUserId(servletRequest, request.creatorUserId());
        CreateMatchRequest command = new CreateMatchRequest(request.bookingOrderId(), creatorUserId, request.minPlayers(), request.maxPlayers());
        return ApiResponse.ok(MatchResponse.from(matchService.create(command)));
    }

    @PostMapping("/{matchId}/join")
    public ApiResponse<MatchResponse> join(@PathVariable Long matchId, @Valid @RequestBody JoinMatchRequest request, HttpServletRequest servletRequest) {
        Long userId = userContext.resolveUserId(servletRequest, request.userId());
        return ApiResponse.ok(MatchResponse.from(matchService.join(matchId, new JoinMatchRequest(userId))));
    }

    @PostMapping("/{matchId}/leave")
    public ApiResponse<MatchResponse> leave(@PathVariable Long matchId, @Valid @RequestBody JoinMatchRequest request, HttpServletRequest servletRequest) {
        Long userId = userContext.resolveUserId(servletRequest, request.userId());
        return ApiResponse.ok(MatchResponse.from(matchService.leave(matchId, new JoinMatchRequest(userId))));
    }

    @PostMapping("/{matchId}/cancel-not-formed")
    public ApiResponse<MatchResponse> cancelNotFormed(@PathVariable Long matchId, HttpServletRequest request) {
        return userContext.currentUserId(request)
                .map(userId -> ApiResponse.ok(MatchResponse.from(matchService.cancelNotFormedForCreator(matchId, userId))))
                .orElseGet(() -> ApiResponse.ok(MatchResponse.from(matchService.cancelNotFormed(matchId))));
    }
}
