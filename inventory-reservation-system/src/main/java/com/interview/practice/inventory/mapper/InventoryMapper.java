package com.interview.practice.inventory.mapper;

import com.interview.practice.inventory.dto.InventoryResponse;
import com.interview.practice.inventory.dto.ReservationResponse;
import com.interview.practice.inventory.model.InventoryItem;
import com.interview.practice.inventory.model.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between entities and DTOs
 */
@Mapper(componentModel = "spring")
public interface InventoryMapper {

    /**
     * Convert InventoryItem entity to InventoryResponse DTO
     */
    InventoryResponse toInventoryResponse(InventoryItem inventoryItem);

    /**
     * Convert Reservation entity to ReservationResponse DTO
     */
    @Mapping(source = "inventoryItem.sku", target = "sku")
    @Mapping(source = "inventoryItem.productName", target = "productName")
    @Mapping(target = "status", expression = "java(reservation.getStatus().name())")
    ReservationResponse toReservationResponse(Reservation reservation);
}

