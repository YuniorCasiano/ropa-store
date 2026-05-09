// ============================================================
// OrderStatus.java - Estados posibles de un pedido
// Un pedido pasa por estos estados en su ciclo de vida.
// ============================================================

package com.modex.orderservice.model;

public enum OrderStatus {

    // El pedido fue creado y esta esperando confirmacion
    // del Inventory Service via Kafka.
    PENDING,

    // El Inventory Service confirmo que hay stock disponible
    // y lo reservo exitosamente.
    CONFIRMED,

    // El pedido fue cancelado. Puede ser porque:
    // - No hay stock disponible
    // - El usuario cancelo manualmente
    // - Hubo un error en el proceso
    CANCELLED
}