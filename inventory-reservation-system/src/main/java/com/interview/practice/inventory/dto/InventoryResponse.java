package com.interview.practice.inventory.dto;

import lombok.*;

/**
 * Response DTO for inventory information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {
    private String sku;
    private String productName;
    private Integer availableQuantity;
    private Integer totalQuantity;
    private Integer reservedQuantity;
}

