// ============================================================
// StockFailedEvent.java - Evento recibido de Kafka
// El Inventory Service publica este evento cuando
// no pudo reservar el stock.
// El Order Service lo escucha y cancela el pedido.
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
public class StockFailedEvent {

    // ID del pedido a cancelar
    private Long orderId;

    // Razon por la que fallo la reserva de stock
    // Ejemplo: "Stock insuficiente para talla M"
    private String reason;
}