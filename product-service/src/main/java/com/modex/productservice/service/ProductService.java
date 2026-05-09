// ============================================================
// ProductService.java - Logica de negocio del Product Service
// Maneja todas las operaciones de productos con cache Redis.
//
// @Cacheable - guarda el resultado en Redis
// @CacheEvict - elimina el cache cuando el producto cambia
// ============================================================

package com.modex.productservice.service;

import com.modex.productservice.dto.CreateProductDTO;
import com.modex.productservice.dto.ProductResponseDTO;
import com.modex.productservice.dto.UpdateProductDTO;
import com.modex.productservice.exception.ProductNotFoundException;
import com.modex.productservice.mapper.ProductMapper;
import com.modex.productservice.model.Product;
import com.modex.productservice.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // ── METODO 1: Crear producto ──────────────────────────────
    // Crea un producto nuevo en MongoDB.
    // @CacheEvict limpia el cache de todos los productos
    // para que la proxima lista incluya el nuevo producto.
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponseDTO createProduct(CreateProductDTO dto) {

        log.debug("Creando producto: {}", dto.name());

        if (productRepository.existsByNameAndActiveTrue(dto.name())) {
            throw new IllegalArgumentException(
                    "Ya existe un producto con el nombre: " + dto.name()
            );
        }

        Product product = ProductMapper.toModel(dto);
        Product saved = productRepository.save(product);

        log.info("Producto creado: {}", saved.getId());

        return ProductMapper.toResponseDTO(saved);
    }

    // ── METODO 2: Obtener producto por ID ─────────────────────
    // @Cacheable guarda el resultado en Redis con clave
    // "products::id_del_producto".
    // La segunda vez que alguien busca el mismo ID,
    // Spring devuelve el resultado de Redis sin ir a MongoDB.
    @Cacheable(value = "products", key = "#id")
    public ProductResponseDTO getProductById(String id) {

        log.debug("Buscando producto por id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        return ProductMapper.toResponseDTO(product);
    }

    // ── METODO 3: Listar todos los productos activos ──────────
    // @Cacheable guarda la lista completa en Redis.
    // La clave es "products::all"
    @Cacheable(value = "products", key = "'all'")
    public List<ProductResponseDTO> getAllProducts() {

        log.debug("Obteniendo todos los productos activos");

        return productRepository.findByActiveTrue()
                .stream()
                .map(ProductMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ── METODO 4: Listar por categoria ────────────────────────
    @Cacheable(value = "products", key = "'category_' + #category")
    public List<ProductResponseDTO> getProductsByCategory(
            String category) {

        log.debug("Buscando productos por categoria: {}", category);

        return productRepository
                .findByCategoryAndActiveTrue(category)
                .stream()
                .map(ProductMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ── METODO 5: Listar por talla ────────────────────────────
    @Cacheable(value = "products", key = "'size_' + #size")
    public List<ProductResponseDTO> getProductsBySize(String size) {

        log.debug("Buscando productos por talla: {}", size);

        return productRepository
                .findByAvailableSizesContaining(size)
                .stream()
                .map(ProductMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ── METODO 6: Actualizar producto ─────────────────────────
    // @CacheEvict elimina el cache de ese producto especifico
    // y el cache de todos los listados para que esten
    // actualizados con los nuevos datos.
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponseDTO updateProduct(String id,
                                            UpdateProductDTO dto) {

        log.debug("Actualizando producto: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        ProductMapper.applyUpdates(product, dto);
        Product updated = productRepository.save(product);

        log.info("Producto actualizado: {}", id);

        return ProductMapper.toResponseDTO(updated);
    }

    // ── METODO 7: Desactivar producto ─────────────────────────
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void deactivateProduct(String id) {

        log.debug("Desactivando producto: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setActive(false);
        productRepository.save(product);

        log.info("Producto desactivado: {}", id);
    }
}