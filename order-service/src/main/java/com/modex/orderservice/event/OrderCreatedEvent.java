// ============================================================
// OrderCreatedEvent.java - Evento publicado en Kafka
// Cuando se crea un pedido el Order Service publica este
// evento en el topic "order.created" de Kafka.
// El Inventory Service lo escucha y reserva el stock.
//
// Este es el corazon del Patron Saga — la comunicacion
// entre servicios a traves de eventos en vez de llamadas
// directas HTTP.
// ============================================================

package com.modex.orderservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// No usamos record aqui porque Kafka necesita poder
// deserializar este objeto desde JSON y los records
// tienen limitaciones con algunos deserializadores.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {

    // ID del pedido — el Inventory Service lo usa para
    // responder con StockReservedEvent o StockFailedEvent
    private Long orderId;

    // ID del producto para verificar stock
    private String productId;

    // Talla especifica que se necesita reservar
    private String size;

    // Cantidad de unidades a reservar
    private Integer quantity;

    // ID del usuario — para notificaciones futuras
    private String userId;
}