package com.ballsaas.match;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRoomRepository extends JpaRepository<MatchRoom, Long> {

    List<MatchRoom> findByStatusAndStartAtBefore(MatchStatus status, OffsetDateTime before);

    List<MatchRoom> findByStatusInAndStartAtBefore(Collection<MatchStatus> statuses, OffsetDateTime before);

    List<MatchRoom> findByVenueIdOrderByStartAtDesc(Long venueId);

    List<MatchRoom> findByCreatorUserIdOrderByStartAtDesc(Long creatorUserId);
}
