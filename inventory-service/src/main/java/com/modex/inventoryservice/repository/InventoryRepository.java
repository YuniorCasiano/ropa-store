package com.modex.inventoryservice.repository;

import com.modex.inventoryservice.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    // Busca el inventario de un producto por talla especifica
    // Se usa para verificar stock cuando llega un OrderCreatedEvent
    Optional<Inventory> findByProductIdAndSize(String productId, String size);

    // Lista todo el inventario de un producto
    List<Inventory> findByProductId(String productId);

    // Verifica si existe inventario para ese producto y talla
    boolean existsByProductIdAndSize(String productId, String size);
}