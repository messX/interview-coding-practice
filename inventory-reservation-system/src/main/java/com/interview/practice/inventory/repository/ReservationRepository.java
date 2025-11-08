package com.interview.practice.inventory.repository;

import com.interview.practice.inventory.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Reservation entity
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByReservationId(String reservationId);

    /**
     * Find expired active reservations
     * Useful for cleanup job
     */
    @Query("SELECT r FROM Reservation r WHERE r.status = 'ACTIVE' AND r.expiresAt < :now")
    List<Reservation> findExpiredReservations(@Param("now") LocalDateTime now);

    /**
     * Find all reservations for a specific inventory item
     */
    @Query("SELECT r FROM Reservation r WHERE r.inventoryItem.sku = :sku AND r.status = :status")
    List<Reservation> findAllBySkuAndStatus(@Param("sku") String sku, @Param("status") Reservation.ReservationStatus status);
}

