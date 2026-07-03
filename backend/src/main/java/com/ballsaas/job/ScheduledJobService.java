package com.ballsaas.job;

import com.ballsaas.booking.BookingService;
import com.ballsaas.match.MatchLifecycleService;
import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledJobService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledJobService.class);

    private final BookingService bookingService;
    private final MatchLifecycleService matchLifecycleService;

    public ScheduledJobService(BookingService bookingService, MatchLifecycleService matchLifecycleService) {
        this.bookingService = bookingService;
        this.matchLifecycleService = matchLifecycleService;
    }

    @Scheduled(fixedDelayString = "${ballsaas.jobs.close-timeout-orders-ms:60000}")
    public void closeTimeoutOrders() {
        int closed = bookingService.closeTimeoutOrders(OffsetDateTime.now());
        if (closed > 0) {
            log.info("closed {} timeout booking orders", closed);
        }
    }

    @Scheduled(fixedDelayString = "${ballsaas.jobs.cancel-unformed-matches-ms:60000}")
    public void cancelUnformedMatches() {
        int cancelled = matchLifecycleService.cancelUnformedMatches();
        if (cancelled > 0) {
            log.info("cancelled {} unformed matches", cancelled);
        }
    }

    @Scheduled(cron = "${ballsaas.jobs.daily-settlement-cron:0 10 2 * * *}")
    public void generateDailySettlement() {
        log.debug("daily settlement job tick");
    }
}