// ============================================================
// Order.java - Entidad de pedido en PostgreSQL
// Representa un pedido de ropa en la base de datos.
// Usa @Entity de JPA en vez de @Document de MongoDB porque
// los pedidos necesitan transacciones estrictas.
// ============================================================

package com.modex.orderservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// @Entity - marca esta clase como una entidad JPA.
// JPA creara una tabla "orders" en PostgreSQL automaticamente.
@Entity
// @Table define el nombre de la tabla en PostgreSQL.
// Usamos "orders" porque "order" es una palabra reservada en SQL.
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    // @Id - clave primaria de la tabla
    // @GeneratedValue - PostgreSQL genera el ID automaticamente
    // IDENTITY usa el autoincrement de PostgreSQL
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID del usuario que hizo el pedido.
    // No es una foreign key porque el usuario vive en
    // el User Service con su propia base de datos.
    // En microservicios cada servicio es dueno de sus datos.
    @Column(name = "user_id", nullable = false)
    private String userId;

    // ID del producto pedido
    @Column(name = "product_id", nullable = false)
    private String productId;

    // Nombre del producto al momento del pedido.
    // Lo guardamos aqui para no depender del Product Service
    // cuando mostramos el historial de pedidos.
    @Column(name = "product_name", nullable = false)
    private String productName;

    // Talla seleccionada por el usuario
    @Column(name = "size", nullable = false)
    private String size;

    // Color seleccionado por el usuario
    @Column(name = "color")
    private String color;

    // Cantidad de unidades pedidas
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // Precio unitario al momento del pedido.
    // Lo guardamos aqui porque el precio puede cambiar
    // en el futuro y el historial debe mostrar el precio
    // que pago el usuario, no el precio actual.
    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    // Precio total = quantity * unitPrice
    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    // Estado del pedido — cambia a medida que Kafka
    // procesa los eventos del Inventory Service.
    // @Enumerated(STRING) guarda el nombre del enum en vez
    // del numero — mas legible en la base de datos.
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    // Razon de cancelacion — solo se llena si status = CANCELLED
    @Column(name = "cancellation_reason")
    private String cancellationReason;

    // Direccion de envio del usuario al momento del pedido
    @Column(name = "shipping_address")
    private String shippingAddress;

    // Fecha de creacion del pedido
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Fecha de ultima actualizacion
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // @PrePersist se ejecuta automaticamente ANTES de que
    // JPA guarde el objeto en PostgreSQL por primera vez.
    // Usamos esto para llenar createdAt automaticamente.
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = OrderStatus.PENDING;
        }
    }

    // @PreUpdate se ejecuta automaticamente ANTES de que
    // JPA actualice el objeto en PostgreSQL.
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}