package com.modex.inventoryservice.dto;

import java.time.LocalDateTime;

public record InventoryResponseDTO(
        Long id,
        String productId,
        String size,
        Integer quantity,
        Integer reservedQuantity,
        Integer availableQuantity,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}