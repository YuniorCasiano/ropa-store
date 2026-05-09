// ============================================================
// OrderService.java - Logica de negocio del Order Service
// Crea pedidos, publica eventos en Kafka y escucha
// respuestas del Inventory Service.
// ============================================================

package com.modex.orderservice.service;

import com.modex.orderservice.dto.CreateOrderDTO;
import com.modex.orderservice.dto.OrderResponseDTO;
import com.modex.orderservice.event.OrderCreatedEvent;
import com.modex.orderservice.event.StockFailedEvent;
import com.modex.orderservice.event.StockReservedEvent;
import com.modex.orderservice.exception.OrderNotFoundException;
import com.modex.orderservice.model.Order;
import com.modex.orderservice.model.OrderStatus;
import com.modex.orderservice.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    // KafkaTemplate es el objeto que usamos para publicar
    // eventos en Kafka. Spring lo crea automaticamente
    // basandose en la configuracion del application.properties.
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // ── METODO 1: Crear pedido ────────────────────────────────
    // Crea el pedido en PostgreSQL con estado PENDING
    // y publica OrderCreatedEvent en Kafka.
    // El Inventory Service lo escucha y reserva el stock.
    @Transactional
    public OrderResponseDTO createOrder(CreateOrderDTO dto,
                                        String userId) {

        log.debug("Creando pedido para usuario: {}", userId);

        // Calculamos el precio total
        BigDecimal totalPrice = dto.unitPrice()
                .multiply(BigDecimal.valueOf(dto.quantity()));

        // Creamos el pedido con estado PENDING
        Order order = Order.builder()
                .userId(userId)
                .productId(dto.productId())
                .productName(dto.productName())
                .size(dto.size())
                .color(dto.color())
                .quantity(dto.quantity())
                .unitPrice(dto.unitPrice())
                .totalPrice(totalPrice)
                .status(OrderStatus.PENDING)
                .shippingAddress(dto.shippingAddress())
                .build();

        // Guardamos en PostgreSQL
        Order saved = orderRepository.save(order);

        // Publicamos el evento en Kafka para que el
        // Inventory Service reserve el stock.
        // "order.created" es el nombre del topic.
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(saved.getId())
                .productId(saved.getProductId())
                .size(saved.getSize())
                .quantity(saved.getQuantity())
                .userId(saved.getUserId())
                .build();

        // kafkaTemplate.send(topic, key, value)
        // - topic: donde publicar el evento
        // - key: el ID del pedido como String
        // - value: el evento como objeto Java (se convierte a JSON)
        kafkaTemplate.send("order.created",
                saved.getId().toString(), event);

        log.info("Pedido creado con id: {} y evento publicado en Kafka",
                saved.getId());

        return toResponseDTO(saved);
    }

    // ── METODO 2: Obtener pedido por ID ───────────────────────
    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return toResponseDTO(order);
    }

    // ── METODO 3: Obtener pedidos del usuario ─────────────────
    public List<OrderResponseDTO> getOrdersByUser(String userId) {
        return orderRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ── METODO 4: Cancelar pedido manualmente ─────────────────
    @Transactional
    public OrderResponseDTO cancelOrder(Long id, String userId) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        // Solo el dueno del pedido puede cancelarlo
        if (!order.getUserId().equals(userId)) {
            throw new IllegalArgumentException(
                    "No tienes permiso para cancelar este pedido");
        }

        // Solo se pueden cancelar pedidos PENDING
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Solo se pueden cancelar pedidos en estado PENDING");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancellationReason("Cancelado por el usuario");
        Order saved = orderRepository.save(order);

        log.info("Pedido {} cancelado por el usuario", id);

        return toResponseDTO(saved);
    }

    // ── KAFKA LISTENER 1: Stock reservado ────────────────────
    // Escucha el topic "inventory.stock.reserved".
    // Cuando el Inventory Service reserva el stock exitosamente
    // este metodo se ejecuta automaticamente y confirma el pedido.
    //
    // @KafkaListener le dice a Spring que este metodo debe
    // ejecutarse cada vez que llega un mensaje al topic especificado.
    @KafkaListener(
            topics = "inventory.stock.reserved",
            groupId = "order-service-group"
    )
    @Transactional
    public void handleStockReserved(StockReservedEvent event) {

        log.info("Stock reservado para pedido: {}", event.getOrderId());

        orderRepository.findById(event.getOrderId())
                .ifPresent(order -> {
                    order.setStatus(OrderStatus.CONFIRMED);
                    orderRepository.save(order);
                    log.info("Pedido {} CONFIRMADO", event.getOrderId());
                });
    }

    // ── KAFKA LISTENER 2: Stock fallido ──────────────────────
    // Escucha el topic "inventory.stock.failed".
    // Cuando el Inventory Service no puede reservar el stock
    // este metodo cancela el pedido automaticamente.
    @KafkaListener(
            topics = "inventory.stock.failed",
            groupId = "order-service-group"
    )
    @Transactional
    public void handleStockFailed(StockFailedEvent event) {

        log.warn("Stock fallido para pedido: {}", event.getOrderId());

        orderRepository.findById(event.getOrderId())
                .ifPresent(order -> {
                    order.setStatus(OrderStatus.CANCELLED);
                    order.setCancellationReason(event.getReason());
                    orderRepository.save(order);
                    log.warn("Pedido {} CANCELADO: {}",
                            event.getOrderId(), event.getReason());
                });
    }

    // ── METODO PRIVADO: toResponseDTO ─────────────────────────
    // Convierte un Order en OrderResponseDTO.
    // Lo hacemos aqui en vez de en un Mapper separado
    // porque el Order Service no tiene tantas conversiones.
    private OrderResponseDTO toResponseDTO(Order order) {
        return new OrderResponseDTO(
                order.getId(),
                order.getUserId(),
                order.getProductId(),
                order.getProductName(),
                order.getSize(),
                order.getColor(),
                order.getQuantity(),
                order.getUnitPrice(),
                order.getTotalPrice(),
                order.getStatus(),
                order.getCancellationReason(),
                order.getShippingAddress(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}