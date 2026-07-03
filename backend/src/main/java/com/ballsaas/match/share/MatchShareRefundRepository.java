package com.ballsaas.match.share;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchShareRefundRepository extends JpaRepository<MatchShareRefund, Long> {

    List<MatchShareRefund> findByMatchRoomId(Long matchRoomId);
}
