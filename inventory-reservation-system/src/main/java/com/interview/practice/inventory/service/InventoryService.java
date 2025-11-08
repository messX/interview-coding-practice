package com.interview.practice.inventory.service;

import com.interview.practice.inventory.dto.InventoryResponse;
import com.interview.practice.inventory.dto.ReservationRequest;
import com.interview.practice.inventory.dto.ReservationResponse;
import com.interview.practice.inventory.exception.InsufficientInventoryException;
import com.interview.practice.inventory.exception.InventoryNotFoundException;
import com.interview.practice.inventory.exception.ReservationNotFoundException;
import com.interview.practice.inventory.mapper.InventoryMapper;
import com.interview.practice.inventory.model.InventoryItem;
import com.interview.practice.inventory.model.Reservation;
import com.interview.practice.inventory.repository.InventoryItemRepository;
import com.interview.practice.inventory.repository.ReservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Inventory Service
 * Handles reservation, release, and inventory management with concurrency control
 */
@Service
@Slf4j
public class InventoryService {

    private static final int DEFAULT_RESERVATION_TIMEOUT_MINUTES = 15;

    @Autowired
    private InventoryItemRepository inventoryRepository;
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private InventoryMapper inventoryMapper;

    /**
     * Get available inventory for a SKU
     */
    @Transactional(readOnly = true)
    public Integer getAvailableInventory(String sku) {
        return inventoryRepository.findBySku(sku)
                .map(InventoryItem::getAvailableQuantity)
                .orElse(0);
    }

    /**
     * Get inventory details with DTO mapping
     */
    @Transactional(readOnly = true)
    public Optional<InventoryResponse> getInventory(String sku) {
        return inventoryRepository.findBySku(sku)
                .map(inventoryMapper::toInventoryResponse);
    }

    /**
     * Reserve inventory with pessimistic locking to prevent race conditions
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ReservationResponse reserveInventory(ReservationRequest reservationRequest) {
        log.info("Attempting to reserve {} units of SKU: {}", 
                reservationRequest.getQuantity(), reservationRequest.getSku());
        
        // Use pessimistic write lock to prevent concurrent modifications
        InventoryItem inventoryItem = inventoryRepository
                .findBySkuWithLock(reservationRequest.getSku())
                .orElseThrow(() -> new InventoryNotFoundException(
                        "Inventory not found for SKU: " + reservationRequest.getSku()));
        
        // Check if sufficient inventory is available
        if (inventoryItem.getAvailableQuantity() < reservationRequest.getQuantity()) {
            log.warn("Insufficient inventory for SKU: {}. Available: {}, Requested: {}", 
                    reservationRequest.getSku(), 
                    inventoryItem.getAvailableQuantity(), 
                    reservationRequest.getQuantity());
            throw new InsufficientInventoryException(
                    String.format("Insufficient inventory. Available: %d, Requested: %d", 
                            inventoryItem.getAvailableQuantity(), 
                            reservationRequest.getQuantity()));
        }
        
        // Update inventory quantities
        inventoryItem.setAvailableQuantity(
                inventoryItem.getAvailableQuantity() - reservationRequest.getQuantity());
        inventoryItem.setReservedQuantity(
                inventoryItem.getReservedQuantity() + reservationRequest.getQuantity());
        inventoryRepository.save(inventoryItem);
        
        // Determine reservation timeout
        int timeoutMinutes = reservationRequest.getTimeoutMinutes() != null 
                ? reservationRequest.getTimeoutMinutes() 
                : DEFAULT_RESERVATION_TIMEOUT_MINUTES;
        
        // Create reservation
        Reservation reservation = Reservation.builder()
                .reservationId(UUID.randomUUID().toString())
                .inventoryItem(inventoryItem)
                .quantity(reservationRequest.getQuantity())
                .status(Reservation.ReservationStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusMinutes(timeoutMinutes))
                .build();
        
        reservationRepository.save(reservation);
        
        log.info("Successfully created reservation: {} for SKU: {}", 
                reservation.getReservationId(), reservationRequest.getSku());
        
        return inventoryMapper.toReservationResponse(reservation);
    }

    /**
     * Release a reservation (explicit release by user)
     * Restores inventory quantities and marks reservation as RELEASED
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ReservationResponse releaseReservation(String reservationId) {
        log.info("Releasing reservation: {}", reservationId);
        
        // Find the reservation
        Reservation reservation = reservationRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Reservation not found: " + reservationId));
        
        // Check if reservation can be released (must be ACTIVE)
        if (reservation.getStatus() != Reservation.ReservationStatus.ACTIVE) {
            log.warn("Cannot release reservation {} with status: {}", 
                    reservationId, reservation.getStatus());
            throw new IllegalStateException(
                    String.format("Cannot release reservation with status: %s. Only ACTIVE reservations can be released.", 
                            reservation.getStatus()));
        }
        
        // Get inventory item with pessimistic lock to prevent concurrent modifications
        InventoryItem inventoryItem = inventoryRepository
                .findBySkuWithLock(reservation.getInventoryItem().getSku())
                .orElseThrow(() -> new InventoryNotFoundException(
                        "Inventory not found for SKU: " + reservation.getInventoryItem().getSku()));
        
        // Restore inventory quantities
        int releasedQuantity = reservation.getQuantity();
        inventoryItem.setAvailableQuantity(
                inventoryItem.getAvailableQuantity() + releasedQuantity);
        inventoryItem.setReservedQuantity(
                inventoryItem.getReservedQuantity() - releasedQuantity);
        
        inventoryRepository.save(inventoryItem);
        
        // Update reservation status
        reservation.setStatus(Reservation.ReservationStatus.RELEASED);
        reservationRepository.save(reservation);
        
        log.info("Successfully released reservation: {}. Restored {} units of SKU: {}", 
                reservationId, releasedQuantity, inventoryItem.getSku());
        
        return inventoryMapper.toReservationResponse(reservation);
    }

    /**
     * Confirm a reservation (convert to order)
     * Consumes the inventory and marks reservation as CONFIRMED
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ReservationResponse confirmReservation(String reservationId, String orderId) {
        log.info("Confirming reservation: {} for order: {}", reservationId, orderId);
        
        // Find the reservation
        Reservation reservation = reservationRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Reservation not found: " + reservationId));
        
        // Check if reservation can be confirmed (must be ACTIVE)
        if (reservation.getStatus() != Reservation.ReservationStatus.ACTIVE) {
            log.warn("Cannot confirm reservation {} with status: {}", 
                    reservationId, reservation.getStatus());
            throw new IllegalStateException(
                    String.format("Cannot confirm reservation with status: %s. Only ACTIVE reservations can be confirmed.", 
                            reservation.getStatus()));
        }
        
        // Get inventory item with pessimistic lock
        InventoryItem inventoryItem = inventoryRepository
                .findBySkuWithLock(reservation.getInventoryItem().getSku())
                .orElseThrow(() -> new InventoryNotFoundException(
                        "Inventory not found for SKU: " + reservation.getInventoryItem().getSku()));
        
        // Consume the inventory (decrease total and reserved, available stays same)
        int confirmedQuantity = reservation.getQuantity();
        inventoryItem.setTotalQuantity(
                inventoryItem.getTotalQuantity() - confirmedQuantity);
        inventoryItem.setReservedQuantity(
                inventoryItem.getReservedQuantity() - confirmedQuantity);
        // availableQuantity stays the same (was already reduced during reservation)
        
        inventoryRepository.save(inventoryItem);
        
        // Update reservation status and link to order
        reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
        reservation.setOrderId(orderId);
        reservationRepository.save(reservation);
        
        log.info("Successfully confirmed reservation: {} for order: {}. Consumed {} units of SKU: {}", 
                reservationId, orderId, confirmedQuantity, inventoryItem.getSku());
        
        return inventoryMapper.toReservationResponse(reservation);
    }

    /**
     * Cleanup expired reservations (scheduled job)
     * Runs periodically to release expired ACTIVE reservations
     */
    @Transactional
    public void cleanupExpiredReservations() {
        log.debug("Running expired reservations cleanup job");
        
        List<Reservation> expiredReservations = reservationRepository
                .findExpiredReservations(LocalDateTime.now());
        
        if (expiredReservations.isEmpty()) {
            log.debug("No expired reservations found");
            return;
        }
        
        log.info("Found {} expired reservations to cleanup", expiredReservations.size());
        
        int successCount = 0;
        int failCount = 0;
        
        for (Reservation reservation : expiredReservations) {
            try {
                // Get inventory item with lock
                InventoryItem inventoryItem = inventoryRepository
                        .findBySkuWithLock(reservation.getInventoryItem().getSku())
                        .orElseThrow(() -> new InventoryNotFoundException(
                                "Inventory not found for SKU: " + reservation.getInventoryItem().getSku()));
                
                // Restore inventory quantities
                int expiredQuantity = reservation.getQuantity();
                inventoryItem.setAvailableQuantity(
                        inventoryItem.getAvailableQuantity() + expiredQuantity);
                inventoryItem.setReservedQuantity(
                        inventoryItem.getReservedQuantity() - expiredQuantity);
                
                inventoryRepository.save(inventoryItem);
                
                // Update reservation status to EXPIRED
                reservation.setStatus(Reservation.ReservationStatus.EXPIRED);
                reservationRepository.save(reservation);
                
                log.debug("Cleaned up expired reservation: {}. Restored {} units of SKU: {}", 
                        reservation.getReservationId(), expiredQuantity, inventoryItem.getSku());
                
                successCount++;
                
            } catch (Exception e) {
                log.error("Failed to cleanup expired reservation: {}", 
                        reservation.getReservationId(), e);
                failCount++;
            }
        }
        
        log.info("Cleanup job completed. Success: {}, Failed: {}", successCount, failCount);
    }
}
