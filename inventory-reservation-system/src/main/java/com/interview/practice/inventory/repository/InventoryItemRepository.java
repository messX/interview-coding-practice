package com.interview.practice.inventory.repository;

import com.interview.practice.inventory.model.InventoryItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for InventoryItem entity
 * Provides database access with locking support
 */
@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findBySku(String sku);

    /**
     * Find inventory item with pessimistic write lock
     * Use this to prevent concurrent modifications (race conditions)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryItem i WHERE i.sku = :sku")
    Optional<InventoryItem> findBySkuWithLock(@Param("sku") String sku);

    /**
     * Find inventory item with optimistic lock
     * Uses @Version field for concurrent control
     */
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT i FROM InventoryItem i WHERE i.sku = :sku")
    Optional<InventoryItem> findBySkuWithOptimisticLock(@Param("sku") String sku);
}

