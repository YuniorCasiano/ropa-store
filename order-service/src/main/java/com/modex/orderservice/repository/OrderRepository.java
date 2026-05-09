// ============================================================
// OrderRepository.java - Repositorio de pedidos
// Usa JpaRepository en vez de MongoRepository porque
// los pedidos viven en PostgreSQL.
//
// JpaRepository<Order, Long>:
// - Order: el tipo de entidad
// - Long: el tipo del @Id
// ============================================================

package com.modex.orderservice.repository;

import com.modex.orderservice.model.Order;
import com.modex.orderservice.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Busca todos los pedidos de un usuario
    // Se usa para mostrar el historial de pedidos
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);

    // Busca pedidos por estado
    // Se usa para listar pedidos pendientes, confirmados, etc.
    List<Order> findByStatus(OrderStatus status);

    // Busca pedidos de un usuario por estado
    List<Order> findByUserIdAndStatus(String userId,
                                      OrderStatus status);
}