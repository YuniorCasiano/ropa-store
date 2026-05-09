// ============================================================
// UpdateProductDTO.java - DTO de actualizacion de producto
// Define que datos puede modificar un administrador.
// Todos los campos son opcionales — solo se actualizan
// los que lleguen en el JSON.
//
// Ejemplo de JSON que recibe:
// {
//   "price": 24.99,
//   "stock": 30
// }
// ============================================================

package com.modex.productservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.util.List;

public record UpdateProductDTO(

        // Todos los campos son opcionales — sin @NotBlank
        // El ProductMapper solo actualiza los que no son null

        String name,

        String description,

        // Si manda precio debe ser mayor que 0
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor que 0")
        BigDecimal price,

        String category,

        String brand,

        List<String> availableSizes,

        List<String> availableColors,

        String imageUrl,

        // Si manda stock debe ser mayor o igual a 0
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock

) {}