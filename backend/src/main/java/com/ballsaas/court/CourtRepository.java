package com.ballsaas.court;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourtRepository extends JpaRepository<Court, Long> {

    List<Court> findByVenueIdAndDeletedFalse(Long venueId);
}

