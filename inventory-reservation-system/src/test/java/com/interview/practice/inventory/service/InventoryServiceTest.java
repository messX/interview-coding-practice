package com.interview.practice.inventory.service;

import com.interview.practice.inventory.dto.ReservationRequest;
import com.interview.practice.inventory.dto.ReservationResponse;
import com.interview.practice.inventory.exception.InsufficientInventoryException;
import com.interview.practice.inventory.exception.InventoryNotFoundException;
import com.interview.practice.inventory.exception.ReservationNotFoundException;
import com.interview.practice.inventory.model.InventoryItem;
import com.interview.practice.inventory.model.Reservation;
import com.interview.practice.inventory.repository.InventoryItemRepository;
import com.interview.practice.inventory.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for InventoryService
 * Tests the reserve inventory flow including concurrency scenarios
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class InventoryServiceTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryItemRepository inventoryRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        // Clean up any existing reservations before each test
        reservationRepository.deleteAll();
    }

    @Test
    void testReserveInventory_Success() {
        // Given
        ReservationRequest request = ReservationRequest.builder()
                .sku("LAPTOP-001")
                .quantity(10)
                .timeoutMinutes(15)
                .build();

        // When
        ReservationResponse response = inventoryService.reserveInventory(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getReservationId());
        assertEquals("LAPTOP-001", response.getSku());
        assertEquals(10, response.getQuantity());
        assertEquals("ACTIVE", response.getStatus());
        assertNotNull(response.getExpiresAt());

        // Verify inventory quantities updated
        InventoryItem item = inventoryRepository.findBySku("LAPTOP-001").orElseThrow();
        assertEquals(90, item.getAvailableQuantity()); // 100 - 10
        assertEquals(10, item.getReservedQuantity()); // 0 + 10

        // Verify reservation created
        Reservation reservation = reservationRepository
                .findByReservationId(response.getReservationId()).orElseThrow();
        assertEquals(Reservation.ReservationStatus.ACTIVE, reservation.getStatus());
        assertEquals(10, reservation.getQuantity());
    }

    @Test
    void testReserveInventory_InsufficientInventory() {
        // Given - request more than available
        ReservationRequest request = ReservationRequest.builder()
                .sku("LAPTOP-001")
                .quantity(150) // Only 100 available
                .build();

        // When/Then
        assertThrows(InsufficientInventoryException.class, () -> {
            inventoryService.reserveInventory(request);
        });

        // Verify no changes to inventory
        InventoryItem item = inventoryRepository.findBySku("LAPTOP-001").orElseThrow();
        assertEquals(100, item.getAvailableQuantity());
        assertEquals(0, item.getReservedQuantity());
    }

    @Test
    void testReserveInventory_InventoryNotFound() {
        // Given
        ReservationRequest request = ReservationRequest.builder()
                .sku("INVALID-SKU")
                .quantity(10)
                .build();

        // When/Then
        assertThrows(InventoryNotFoundException.class, () -> {
            inventoryService.reserveInventory(request);
        });
    }

    @Test
    void testReserveInventory_ExactAvailableQuantity() {
        // Given - reserve exactly what's available
        ReservationRequest request = ReservationRequest.builder()
                .sku("TABLET-001") // Has 50 units
                .quantity(50)
                .build();

        // When
        ReservationResponse response = inventoryService.reserveInventory(request);

        // Then
        assertNotNull(response);
        InventoryItem item = inventoryRepository.findBySku("TABLET-001").orElseThrow();
        assertEquals(0, item.getAvailableQuantity());
        assertEquals(50, item.getReservedQuantity());
    }

    @Test
    void testReserveInventory_MultipleReservations() {
        // Given
        ReservationRequest request1 = ReservationRequest.builder()
                .sku("PHONE-001")
                .quantity(100)
                .build();
        ReservationRequest request2 = ReservationRequest.builder()
                .sku("PHONE-001")
                .quantity(200)
                .build();

        // When
        ReservationResponse response1 = inventoryService.reserveInventory(request1);
        ReservationResponse response2 = inventoryService.reserveInventory(request2);

        // Then
        assertNotNull(response1);
        assertNotNull(response2);
        assertNotEquals(response1.getReservationId(), response2.getReservationId());

        // Verify cumulative quantities
        InventoryItem item = inventoryRepository.findBySku("PHONE-001").orElseThrow();
        assertEquals(200, item.getAvailableQuantity()); // 500 - 100 - 200
        assertEquals(300, item.getReservedQuantity()); // 100 + 200
    }

    @Test
    void testReserveInventory_DefaultTimeout() {
        // Given - no timeout specified
        ReservationRequest request = ReservationRequest.builder()
                .sku("LAPTOP-001")
                .quantity(5)
                .build();

        // When
        ReservationResponse response = inventoryService.reserveInventory(request);

        // Then
        assertNotNull(response.getExpiresAt());
        // Should use default 15 minutes
        assertTrue(response.getExpiresAt().isAfter(
                response.getCreatedAt().plusMinutes(14)));
    }

    @Test
    void testReserveInventory_CustomTimeout() {
        // Given - custom timeout
        ReservationRequest request = ReservationRequest.builder()
                .sku("LAPTOP-001")
                .quantity(5)
                .timeoutMinutes(30)
                .build();

        // When
        ReservationResponse response = inventoryService.reserveInventory(request);

        // Then
        assertNotNull(response.getExpiresAt());
        // Should use custom 30 minutes
        assertTrue(response.getExpiresAt().isAfter(
                response.getCreatedAt().plusMinutes(29)));
    }

    /**
     * Critical concurrency test - prevents overselling
     * This tests the pessimistic locking mechanism
     * Note: Test validates that total reserved doesn't exceed available
     */
    @Test
    void testReserveInventory_ConcurrentReservations_PreventsOverselling() throws InterruptedException {
        // Given - 100 units available, 10 threads trying to reserve 15 each
        // Expected: Total reserved should not exceed 100
        int threadCount = 10;
        int quantityPerReservation = 15;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // When - concurrent reservations
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    ReservationRequest request = ReservationRequest.builder()
                            .sku("LAPTOP-001")
                            .quantity(quantityPerReservation)
                            .build();
                    inventoryService.reserveInventory(request);
                    successCount.incrementAndGet();
                } catch (InsufficientInventoryException e) {
                    failCount.incrementAndGet();
                } catch (Exception e) {
                    // Handle any transaction-related exceptions
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Then
        System.out.println("Concurrent test results:");
        System.out.println("  Successful reservations: " + successCount.get());
        System.out.println("  Failed reservations: " + failCount.get());
        
        // Verify inventory integrity - most important check
        InventoryItem item = inventoryRepository.findBySku("LAPTOP-001").orElseThrow();
        System.out.println("  Final available: " + item.getAvailableQuantity());
        System.out.println("  Final reserved: " + item.getReservedQuantity());
        
        // Key assertion: Total should still be 100 (no overselling)
        assertEquals(100, item.getAvailableQuantity() + item.getReservedQuantity(),
                "Total inventory should remain 100 (available + reserved)");
        
        // Reserved should not exceed original available
        assertTrue(item.getReservedQuantity() <= 100,
                "Reserved quantity should not exceed original available quantity");
        
        // At least some should succeed and some should fail
        assertTrue(successCount.get() > 0, "Expected at least some successful reservations");
        assertTrue(successCount.get() < threadCount, "Not all reservations should succeed");
    }

    // ==================== Release Reservation Tests ====================

    @Test
    void testReleaseReservation_Success() {
        // Given - create a reservation first
        ReservationRequest request = ReservationRequest.builder()
                .sku("LAPTOP-001")
                .quantity(20)
                .build();
        ReservationResponse reservation = inventoryService.reserveInventory(request);

        // Verify inventory after reservation
        InventoryItem itemBefore = inventoryRepository.findBySku("LAPTOP-001").orElseThrow();
        assertEquals(80, itemBefore.getAvailableQuantity()); // 100 - 20
        assertEquals(20, itemBefore.getReservedQuantity());

        // When - release the reservation
        ReservationResponse released = inventoryService.releaseReservation(reservation.getReservationId());

        // Then
        assertNotNull(released);
        assertEquals("RELEASED", released.getStatus());
        assertEquals(20, released.getQuantity());

        // Verify inventory quantities are restored
        InventoryItem itemAfter = inventoryRepository.findBySku("LAPTOP-001").orElseThrow();
        assertEquals(100, itemAfter.getAvailableQuantity()); // 80 + 20
        assertEquals(0, itemAfter.getReservedQuantity()); // 20 - 20

        // Verify reservation status updated
        Reservation updatedReservation = reservationRepository
                .findByReservationId(reservation.getReservationId()).orElseThrow();
        assertEquals(Reservation.ReservationStatus.RELEASED, updatedReservation.getStatus());
    }

    @Test
    void testReleaseReservation_NotFound() {
        // Given - invalid reservation ID
        String invalidId = "invalid-reservation-id";

        // When/Then
        assertThrows(ReservationNotFoundException.class, () -> {
            inventoryService.releaseReservation(invalidId);
        });
    }

    @Test
    void testReleaseReservation_AlreadyReleased() {
        // Given - create and release a reservation
        ReservationRequest request = ReservationRequest.builder()
                .sku("PHONE-001")
                .quantity(50)
                .build();
        ReservationResponse reservation = inventoryService.reserveInventory(request);
        inventoryService.releaseReservation(reservation.getReservationId());

        // When/Then - try to release again
        assertThrows(IllegalStateException.class, () -> {
            inventoryService.releaseReservation(reservation.getReservationId());
        });

        // Verify inventory hasn't changed
        InventoryItem item = inventoryRepository.findBySku("PHONE-001").orElseThrow();
        assertEquals(500, item.getAvailableQuantity()); // Should be restored to original
        assertEquals(0, item.getReservedQuantity());
    }

    @Test
    void testReleaseReservation_MultipleSequential() {
        // Given - create multiple reservations
        ReservationRequest request1 = ReservationRequest.builder()
                .sku("MONITOR-001")
                .quantity(30)
                .build();
        ReservationRequest request2 = ReservationRequest.builder()
                .sku("MONITOR-001")
                .quantity(40)
                .build();

        ReservationResponse res1 = inventoryService.reserveInventory(request1);
        ReservationResponse res2 = inventoryService.reserveInventory(request2);

        // Verify inventory after both reservations
        InventoryItem itemMid = inventoryRepository.findBySku("MONITOR-001").orElseThrow();
        assertEquals(130, itemMid.getAvailableQuantity()); // 200 - 30 - 40
        assertEquals(70, itemMid.getReservedQuantity()); // 30 + 40

        // When - release first reservation
        inventoryService.releaseReservation(res1.getReservationId());

        // Then - verify partial restoration
        InventoryItem itemAfterFirst = inventoryRepository.findBySku("MONITOR-001").orElseThrow();
        assertEquals(160, itemAfterFirst.getAvailableQuantity()); // 130 + 30
        assertEquals(40, itemAfterFirst.getReservedQuantity()); // 70 - 30

        // When - release second reservation
        inventoryService.releaseReservation(res2.getReservationId());

        // Then - verify full restoration
        InventoryItem itemAfterSecond = inventoryRepository.findBySku("MONITOR-001").orElseThrow();
        assertEquals(200, itemAfterSecond.getAvailableQuantity()); // 160 + 40
        assertEquals(0, itemAfterSecond.getReservedQuantity()); // 40 - 40
    }

    @Test
    void testReleaseReservation_ReserveAfterRelease() {
        // Given - create and release a reservation
        ReservationRequest request1 = ReservationRequest.builder()
                .sku("KEYBOARD-001")
                .quantity(60)
                .build();
        ReservationResponse res1 = inventoryService.reserveInventory(request1);
        inventoryService.releaseReservation(res1.getReservationId());

        // When - create new reservation after release
        ReservationRequest request2 = ReservationRequest.builder()
                .sku("KEYBOARD-001")
                .quantity(70)
                .build();
        ReservationResponse res2 = inventoryService.reserveInventory(request2);

        // Then
        assertNotNull(res2);
        assertNotEquals(res1.getReservationId(), res2.getReservationId());

        // Verify inventory
        InventoryItem item = inventoryRepository.findBySku("KEYBOARD-001").orElseThrow();
        assertEquals(80, item.getAvailableQuantity()); // 150 - 70
        assertEquals(70, item.getReservedQuantity());
    }
}

