// ============================================================
// OrderResponseDTO.java - DTO de respuesta de pedido
// Define que datos devuelve la API cuando se consulta
// un pedido.
// ============================================================

package com.modex.orderservice.dto;

import com.modex.orderservice.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponseDTO(

        Long id,
        String userId,
        String productId,
        String productName,
        String size,
        String color,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice,
        OrderStatus status,
        String cancellationReason,
        String shippingAddress,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {}