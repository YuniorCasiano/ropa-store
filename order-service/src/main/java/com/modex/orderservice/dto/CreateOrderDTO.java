// ============================================================
// CreateOrderDTO.java - DTO de creacion de pedido
// Define que datos manda el cliente para crear un pedido.
//
// Ejemplo de JSON que recibe:
// {
//   "productId": "64f1a2b3c4d5e6f7a8b9c0d1",
//   "productName": "Camiseta Negra",
//   "size": "M",
//   "color": "Negro",
//   "quantity": 2,
//   "unitPrice": 29.99,
//   "shippingAddress": "Calle Principal #123"
// }
// ============================================================

package com.modex.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateOrderDTO(

        @NotBlank(message = "El ID del producto es obligatorio")
        String productId,

        @NotBlank(message = "El nombre del producto es obligatorio")
        String productName,

        @NotBlank(message = "La talla es obligatoria")
        String size,

        String color,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser minimo 1")
        Integer quantity,

        @NotNull(message = "El precio unitario es obligatorio")
        BigDecimal unitPrice,

        String shippingAddress

) {}