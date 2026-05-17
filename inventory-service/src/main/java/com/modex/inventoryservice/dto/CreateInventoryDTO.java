package com.modex.inventoryservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateInventoryDTO(

        @NotBlank(message = "El ID del producto es obligatorio")
        String productId,

        @NotBlank(message = "La talla es obligatoria")
        String size,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 0, message = "La cantidad no puede ser negativa")
        Integer quantity
) {}