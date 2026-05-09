// ============================================================
// CreateProductDTO.java - DTO de creacion de producto
// Define que datos manda el cliente para crear un producto.
//
// Ejemplo de JSON que recibe:
// {
//   "name": "Camiseta Negra",
//   "description": "Camiseta de algodon 100%",
//   "price": 29.99,
//   "category": "CAMISETA",
//   "brand": "Zara",
//   "availableSizes": ["S", "M", "L", "XL"],
//   "availableColors": ["Negro", "Blanco"],
//   "imageUrl": "https://...",
//   "stock": 50
// }
// ============================================================

package com.modex.productservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record CreateProductDTO(

        // Nombre del producto — obligatorio
        @NotBlank(message = "El nombre es obligatorio")
        String name,

        // Descripcion — obligatoria
        @NotBlank(message = "La descripcion es obligatoria")
        String description,

        // Precio — obligatorio y mayor que 0
        // @NotNull verifica que no sea null
        // @DecimalMin verifica que sea mayor que 0.01
        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor que 0")
        BigDecimal price,

        // Categoria — obligatoria
        // Ejemplos: CAMISETA, PANTALON, ZAPATOS, ACCESORIO
        @NotBlank(message = "La categoria es obligatoria")
        String category,

        // Marca — obligatoria
        @NotBlank(message = "La marca es obligatoria")
        String brand,

        // Tallas disponibles — obligatorias
        // @NotNull verifica que la lista no sea null
        // puede ser una lista vacia pero no null
        @NotNull(message = "Las tallas son obligatorias")
        List<String> availableSizes,

        // Colores disponibles — obligatorios
        @NotNull(message = "Los colores son obligatorios")
        List<String> availableColors,

        // URL de la imagen — opcional
        String imageUrl,

        // Stock inicial — obligatorio y mayor o igual a 0
        // @Min verifica que sea minimo 0
        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock

) {}