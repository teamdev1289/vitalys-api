package com.vitalys.modules.stability.service;

import com.vitalys.modules.stability.entity.StabilityPullEvent;
import com.vitalys.modules.stability.repository.StabilityPullEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StabilityPullScheduler {

    private final StabilityPullEventRepository pullEventRepository;

    /**
     * Daily background job to scan scheduled pull events due today or earlier,
     * transitioning their status to PENDING_PULL to alert laboratory staff.
     */
    @Scheduled(cron = "0 0 6 * * *") // Daily at 6:00 AM
    @Transactional
    public void scanDuePullEvents() {
        LocalDate today = LocalDate.now();
        List<StabilityPullEvent> dueEvents = pullEventRepository.findDuePullEvents(today);

        for (StabilityPullEvent event : dueEvents) {
            event.setStatus("PENDING_PULL");
            pullEventRepository.save(event);
            log.info("Stability Pull Event [{}] for Study [{}] is now PENDING_PULL (Scheduled: {})",
                    event.getId(), event.getStudyId(), event.getScheduledDate());
        }

        if (!dueEvents.isEmpty()) {
            log.info("StabilityPullScheduler: Flagged {} stability pull events as PENDING_PULL", dueEvents.size());
        }
    }
}
