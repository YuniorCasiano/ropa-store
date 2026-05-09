// ============================================================
// StockReservedEvent.java - Evento recibido de Kafka
// El Inventory Service publica este evento cuando
// reservo el stock exitosamente.
// El Order Service lo escucha y confirma el pedido.
// ============================================================

package com.modex.orderservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockReservedEvent {

    // ID del pedido a confirmar
    private Long orderId;

    // ID del producto cuyo stock fue reservado
    private String productId;

    // Cantidad reservada
    private Integer quantity;
}