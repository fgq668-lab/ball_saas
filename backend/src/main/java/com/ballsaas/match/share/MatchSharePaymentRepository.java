package com.ballsaas.match.share;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchSharePaymentRepository extends JpaRepository<MatchSharePayment, Long> {

    Optional<MatchSharePayment> findByMatchRoomIdAndUserId(Long matchRoomId, Long userId);

    Optional<MatchSharePayment> findByPaymentOrderId(Long paymentOrderId);

    long countByMatchRoomIdAndStatus(Long matchRoomId, MatchSharePaymentStatus status);

    List<MatchSharePayment> findByMatchRoomId(Long matchRoomId);
}