package com.modex.inventoryservice.service;

import com.modex.inventoryservice.dto.CreateInventoryDTO;
import com.modex.inventoryservice.dto.InventoryResponseDTO;
import com.modex.inventoryservice.event.OrderCreatedEvent;
import com.modex.inventoryservice.event.StockFailedEvent;
import com.modex.inventoryservice.event.StockReservedEvent;
import com.modex.inventoryservice.exception.InventoryNotFoundException;
import com.modex.inventoryservice.model.Inventory;
import com.modex.inventoryservice.repository.InventoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // ── METODO 1: Crear inventario ────────────────────────────
    // Crea un registro de inventario para un producto y talla.
    // Lo usa el administrador cuando agrega stock de un producto.
    @Transactional
    public InventoryResponseDTO createInventory(CreateInventoryDTO dto) {

        log.debug("Creando inventario para productId: {} talla: {}",
                dto.productId(), dto.size());

        if (inventoryRepository.existsByProductIdAndSize(
                dto.productId(), dto.size())) {
            throw new IllegalArgumentException(
                    "Ya existe inventario para productId: "
                            + dto.productId() + " talla: " + dto.size());
        }

        Inventory inventory = Inventory.builder()
                .productId(dto.productId())
                .size(dto.size())
                .quantity(dto.quantity())
                .reservedQuantity(0)
                .build();

        Inventory saved = inventoryRepository.save(inventory);
        log.info("Inventario creado: {}", saved.getId());

        return toResponseDTO(saved);
    }

    // ── METODO 2: Obtener inventario por producto ─────────────
    public List<InventoryResponseDTO> getInventoryByProduct(
            String productId) {

        return inventoryRepository.findByProductId(productId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ── KAFKA LISTENER: Escucha OrderCreated ──────────────────
    // Este es el corazon del Patron Saga en el Inventory Service.
    // Cuando el Order Service crea un pedido publica un evento
    // en Kafka. Este metodo lo escucha automaticamente.
    //
    // Si hay stock disponible:
    //   1. Reserva el stock (reduce cantidad disponible)
    //   2. Publica StockReservedEvent → Order Service confirma
    //
    // Si no hay stock:
    //   1. Publica StockFailedEvent → Order Service cancela
    @KafkaListener(
            topics = "order.created",
            groupId = "inventory-service-group"
    )
    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {

        log.info("OrderCreated recibido para orderId: {} productId: {} talla: {}",
                event.getOrderId(), event.getProductId(), event.getSize());

        inventoryRepository.findByProductIdAndSize(
                        event.getProductId(), event.getSize())
                .ifPresentOrElse(
                        inventory -> processStockReservation(inventory, event),
                        () -> publishStockFailed(event,
                                "No existe inventario para productId: "
                                        + event.getProductId()
                                        + " talla: " + event.getSize())
                );
    }

    // ── METODO PRIVADO: Procesar reserva de stock ─────────────
    private void processStockReservation(Inventory inventory,
                                         OrderCreatedEvent event) {

        // Calculamos stock disponible real
        // disponible = total - reservado
        int available = inventory.getQuantity()
                - inventory.getReservedQuantity();

        if (available >= event.getQuantity()) {
            // Hay stock suficiente — reservamos
            inventory.setReservedQuantity(
                    inventory.getReservedQuantity() + event.getQuantity());
            inventoryRepository.save(inventory);

            // Publicamos evento de exito al Order Service
            StockReservedEvent reservedEvent = StockReservedEvent.builder()
                    .orderId(event.getOrderId())
                    .productId(event.getProductId())
                    .quantity(event.getQuantity())
                    .build();

            kafkaTemplate.send("inventory.stock.reserved",
                    event.getOrderId().toString(), reservedEvent);

            log.info("Stock reservado para orderId: {}", event.getOrderId());

        } else {
            // No hay stock suficiente
            publishStockFailed(event,
                    "Stock insuficiente para productId: "
                            + event.getProductId()
                            + " talla: " + event.getSize()
                            + ". Disponible: " + available
                            + " Solicitado: " + event.getQuantity());
        }
    }

    // ── METODO PRIVADO: Publicar fallo de stock ───────────────
    private void publishStockFailed(OrderCreatedEvent event, String reason) {

        StockFailedEvent failedEvent = StockFailedEvent.builder()
                .orderId(event.getOrderId())
                .reason(reason)
                .build();

        kafkaTemplate.send("inventory.stock.failed",
                event.getOrderId().toString(), failedEvent);

        log.warn("Stock fallido para orderId: {} razon: {}",
                event.getOrderId(), reason);
    }

    // ── METODO PRIVADO: Convertir a DTO ───────────────────────
    private InventoryResponseDTO toResponseDTO(Inventory inventory) {
        return new InventoryResponseDTO(
                inventory.getId(),
                inventory.getProductId(),
                inventory.getSize(),
                inventory.getQuantity(),
                inventory.getReservedQuantity(),
                inventory.getQuantity() - inventory.getReservedQuantity(),
                inventory.getCreatedAt(),
                inventory.getUpdatedAt()
        );
    }
}