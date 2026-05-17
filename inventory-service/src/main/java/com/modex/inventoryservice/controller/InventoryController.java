package com.modex.inventoryservice.controller;

import com.modex.inventoryservice.dto.CreateInventoryDTO;
import com.modex.inventoryservice.dto.InventoryResponseDTO;
import com.modex.inventoryservice.service.InventoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // POST /api/inventory — crea stock para un producto y talla
    // Requiere JWT — solo administradores agregan stock
    @PostMapping
    public ResponseEntity<InventoryResponseDTO> createInventory(
            @Valid @RequestBody CreateInventoryDTO dto) {
        log.info("POST /api/inventory - productId: {}", dto.productId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(inventoryService.createInventory(dto));
    }

    // GET /api/inventory/{productId} — consulta stock de un producto
    // Muestra todas las tallas disponibles con su cantidad
    @GetMapping("/{productId}")
    public ResponseEntity<List<InventoryResponseDTO>> getInventoryByProduct(
            @PathVariable String productId) {
        log.info("GET /api/inventory/{}", productId);
        return ResponseEntity.ok(
                inventoryService.getInventoryByProduct(productId));
    }
}