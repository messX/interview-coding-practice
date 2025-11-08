package com.interview.practice.inventory.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * Response DTO for reservation information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {
    private String reservationId;
    private String sku;
    private String productName;
    private Integer quantity;
    private String status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}

