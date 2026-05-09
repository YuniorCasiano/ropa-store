// ============================================================
// ProductResponseDTO.java - DTO de respuesta de producto
// Define que datos devuelve la API cuando alguien consulta
// un producto. Es lo que ve el frontend y el cliente.
//
// Ejemplo de JSON que devuelve:
// {
//   "id": "64f1a2b3c4d5e6f7a8b9c0d1",
//   "name": "Camiseta Negra",
//   "description": "Camiseta de algodon 100%",
//   "price": 29.99,
//   "category": "CAMISETA",
//   "brand": "Zara",
//   "availableSizes": ["S", "M", "L", "XL"],
//   "availableColors": ["Negro", "Blanco"],
//   "imageUrl": "https://...",
//   "stock": 50,
//   "active": true,
//   "createdAt": "2024-01-15T10:30:00"
// }
// ============================================================

package com.modex.productservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProductResponseDTO(

        // ID generado por MongoDB
        String id,

        // Nombre del producto
        String name,

        // Descripcion del producto
        String description,

        // Precio del producto
        BigDecimal price,

        // Categoria de la prenda
        String category,

        // Marca del producto
        String brand,

        // Lista de tallas disponibles
        List<String> availableSizes,

        // Lista de colores disponibles
        List<String> availableColors,

        // URL de la imagen principal
        String imageUrl,

        // Stock disponible
        Integer stock,

        // Si el producto esta activo
        Boolean active,

        // Fecha de creacion
        LocalDateTime createdAt,

        // Fecha de ultima actualizacion
        LocalDateTime updatedAt

) {}