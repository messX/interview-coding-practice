package com.interview.practice.inventory.scheduler;

import com.interview.practice.inventory.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for cleaning up expired reservations
 * Runs every minute to check for and release expired reservations
 */
@Component
@Slf4j
public class ReservationCleanupScheduler {

    @Autowired
    private InventoryService inventoryService;

    /**
     * Cleanup expired reservations every minute
     * fixedRate = 60000ms (1 minute)
     */
    @Scheduled(fixedRate = 60000)
    public void scheduleCleanup() {
        log.trace("Triggering scheduled reservation cleanup");
        inventoryService.cleanupExpiredReservations();
    }
}

