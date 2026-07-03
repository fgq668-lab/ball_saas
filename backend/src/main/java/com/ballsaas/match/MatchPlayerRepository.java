package com.ballsaas.match;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchPlayerRepository extends JpaRepository<MatchPlayer, Long> {

    Optional<MatchPlayer> findByMatchRoomIdAndUserId(Long matchRoomId, Long userId);

    List<MatchPlayer> findByMatchRoomId(Long matchRoomId);

    long countByMatchRoomIdAndStatusNot(Long matchRoomId, MatchPlayerStatus status);
}
