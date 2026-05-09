// ============================================================
// OrderController.java - Endpoints del Order Service
//
// Endpoints:
// POST   /api/orders              -> crear pedido
// GET    /api/orders/{id}         -> obtener pedido por ID
// GET    /api/orders/my-orders    -> pedidos del usuario
// DELETE /api/orders/{id}         -> cancelar pedido
// ============================================================

package com.modex.orderservice.controller;

import com.modex.orderservice.dto.CreateOrderDTO;
import com.modex.orderservice.dto.OrderResponseDTO;
import com.modex.orderservice.service.OrderService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // POST /api/orders — crea un pedido nuevo
    // @AuthenticationPrincipal extrae el usuario autenticado
    // del contexto de Spring Security — el email del JWT.
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(
            @Valid @RequestBody CreateOrderDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        // userDetails.getUsername() devuelve el email
        // que pusimos como subject en el JWT
        String userId = userDetails.getUsername();

        log.info("POST /api/orders - Usuario: {}", userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.createOrder(dto, userId));
    }

    // GET /api/orders/{id} — obtiene un pedido por ID
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @PathVariable Long id) {
        log.info("GET /api/orders/{}", id);
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    // GET /api/orders/my-orders — pedidos del usuario autenticado
    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderResponseDTO>> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = userDetails.getUsername();
        log.info("GET /api/orders/my-orders - Usuario: {}", userId);
        return ResponseEntity.ok(
                orderService.getOrdersByUser(userId));
    }

    // DELETE /api/orders/{id} — cancela un pedido
    @DeleteMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = userDetails.getUsername();
        log.info("DELETE /api/orders/{} - Usuario: {}", id, userId);
        return ResponseEntity.ok(
                orderService.cancelOrder(id, userId));
    }
}