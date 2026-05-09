// ============================================================
// ProductController.java - Endpoints del Product Service
//
// Endpoints:
// GET    /api/products              -> listar todos
// GET    /api/products/{id}         -> obtener por ID
// GET    /api/products/category/{c} -> filtrar por categoria
// GET    /api/products/size/{size}  -> filtrar por talla
// POST   /api/products              -> crear producto
// PUT    /api/products/{id}         -> actualizar producto
// DELETE /api/products/{id}         -> desactivar producto
// ============================================================

package com.modex.productservice.controller;

import com.modex.productservice.dto.CreateProductDTO;
import com.modex.productservice.dto.ProductResponseDTO;
import com.modex.productservice.dto.UpdateProductDTO;
import com.modex.productservice.service.ProductService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // GET /api/products — lista todos los productos activos
    // Publico — no requiere token
    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        log.info("GET /api/products");
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // GET /api/products/{id} — obtiene un producto por ID
    // Publico — no requiere token
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(
            @PathVariable String id) {
        log.info("GET /api/products/{}", id);
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // GET /api/products/category/{category}
    // Filtra productos por categoria — publico
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductResponseDTO>> getByCategory(
            @PathVariable String category) {
        log.info("GET /api/products/category/{}", category);
        return ResponseEntity.ok(
                productService.getProductsByCategory(category));
    }

    // GET /api/products/size/{size}
    // Filtra productos por talla — publico
    @GetMapping("/size/{size}")
    public ResponseEntity<List<ProductResponseDTO>> getBySize(
            @PathVariable String size) {
        log.info("GET /api/products/size/{}", size);
        return ResponseEntity.ok(
                productService.getProductsBySize(size));
    }

    // POST /api/products — crea un producto nuevo
    // Requiere token JWT
    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(
            @Valid @RequestBody CreateProductDTO dto) {
        log.info("POST /api/products - Creando: {}", dto.name());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productService.createProduct(dto));
    }

    // PUT /api/products/{id} — actualiza un producto
    // Requiere token JWT
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody UpdateProductDTO dto) {
        log.info("PUT /api/products/{}", id);
        return ResponseEntity.ok(
                productService.updateProduct(id, dto));
    }

    // DELETE /api/products/{id} — desactiva un producto
    // Requiere token JWT
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateProduct(
            @PathVariable String id) {
        log.info("DELETE /api/products/{}", id);
        productService.deactivateProduct(id);
        return ResponseEntity.noContent().build();
    }
}