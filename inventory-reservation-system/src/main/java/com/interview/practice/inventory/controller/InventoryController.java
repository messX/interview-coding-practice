package com.interview.practice.inventory.controller;

import com.interview.practice.inventory.dto.InventoryResponse;
import com.interview.practice.inventory.dto.ReservationRequest;
import com.interview.practice.inventory.dto.ReservationResponse;
import com.interview.practice.inventory.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for inventory management
 * Uses @Autowired for dependency injection and DTOs for request/response
 */
@RestController
@RequestMapping("/api/inventory")
@Slf4j
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    /**
     * Health check / status endpoint
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> status() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Inventory Reservation System");
        response.put("message", "Ready for implementation with Lombok & MapStruct");
        return ResponseEntity.ok(response);
    }

    /**
     * Get inventory details for a SKU
     */
    @GetMapping("/{sku}")
    public ResponseEntity<InventoryResponse> getInventory(@PathVariable String sku) {
        log.info("Fetching inventory for SKU: {}", sku);
        return inventoryService.getInventory(sku)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Reserve inventory
     */
    @PostMapping("/reserve")
    public ResponseEntity<ReservationResponse> reserveInventory(
            @Validated @RequestBody ReservationRequest request) {
        log.info("Reserve inventory request: SKU={}, quantity={}", request.getSku(), request.getQuantity());
        ReservationResponse response = inventoryService.reserveInventory(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Release a reservation
     */
    @PostMapping("/release/{reservationId}")
    public ResponseEntity<ReservationResponse> releaseReservation(
            @PathVariable String reservationId) {
        log.info("Release reservation request: {}", reservationId);
        ReservationResponse response = inventoryService.releaseReservation(reservationId);
        return ResponseEntity.ok(response);
    }

    /**
     * Confirm a reservation (convert to order)
     */
    @PostMapping("/confirm/{reservationId}")
    public ResponseEntity<ReservationResponse> confirmReservation(
            @PathVariable String reservationId,
            @RequestParam String orderId) {
        log.info("Confirm reservation request: {} for order: {}", reservationId, orderId);
        ReservationResponse response = inventoryService.confirmReservation(reservationId, orderId);
        return ResponseEntity.ok(response);
    }
}


