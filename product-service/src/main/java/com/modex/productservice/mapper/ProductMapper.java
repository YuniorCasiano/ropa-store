// ============================================================
// ProductMapper.java - Conversor entre Model y DTOs
// Convierte Product → ProductResponseDTO y
// Convierte CreateProductDTO → Product
// Aplica actualizaciones parciales con UpdateProductDTO
// ============================================================

package com.modex.productservice.mapper;

import com.modex.productservice.dto.CreateProductDTO;
import com.modex.productservice.dto.ProductResponseDTO;
import com.modex.productservice.dto.UpdateProductDTO;
import com.modex.productservice.model.Product;

public class ProductMapper {

    // Constructor privado — clase de utilidad estatica
    private ProductMapper() {}

    // ── METODO 1: Product → ProductResponseDTO ────────────────
    // Convierte un documento de MongoDB en respuesta HTTP.
    // Se usa en el Service cada vez que devuelves un producto.
    public static ProductResponseDTO toResponseDTO(Product product) {

        if (product == null) {
            return null;
        }

        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getBrand(),
                product.getAvailableSizes(),
                product.getAvailableColors(),
                product.getImageUrl(),
                product.getStock(),
                product.getActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    // ── METODO 2: CreateProductDTO → Product ──────────────────
    // Convierte los datos HTTP en un objeto Product listo
    // para guardar en MongoDB.
    public static Product toModel(CreateProductDTO dto) {

        if (dto == null) {
            return null;
        }

        return Product.builder()
                .name(dto.name())
                .description(dto.description())
                .price(dto.price())
                .category(dto.category())
                .brand(dto.brand())
                .availableSizes(dto.availableSizes())
                .availableColors(dto.availableColors())
                .imageUrl(dto.imageUrl())
                .stock(dto.stock())
                .active(true)
                .build();
    }

    // ── METODO 3: Aplicar UpdateProductDTO sobre un Product ───
    // Actualiza solo los campos que llegaron en el DTO.
    // Si un campo es null significa que el cliente no quiso
    // cambiarlo — lo dejamos como estaba.
    public static Product applyUpdates(Product product,
                                       UpdateProductDTO dto) {

        if (dto == null) {
            return product;
        }

        if (dto.name() != null && !dto.name().isBlank()) {
            product.setName(dto.name());
        }

        if (dto.description() != null && !dto.description().isBlank()) {
            product.setDescription(dto.description());
        }

        if (dto.price() != null) {
            product.setPrice(dto.price());
        }

        if (dto.category() != null && !dto.category().isBlank()) {
            product.setCategory(dto.category());
        }

        if (dto.brand() != null && !dto.brand().isBlank()) {
            product.setBrand(dto.brand());
        }

        if (dto.availableSizes() != null) {
            product.setAvailableSizes(dto.availableSizes());
        }

        if (dto.availableColors() != null) {
            product.setAvailableColors(dto.availableColors());
        }

        if (dto.imageUrl() != null) {
            product.setImageUrl(dto.imageUrl());
        }

        if (dto.stock() != null) {
            product.setStock(dto.stock());
        }

        return product;
    }
}