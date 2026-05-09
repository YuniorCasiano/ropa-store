// ============================================================
// ProductRepository.java - Repositorio de productos
// Maneja todas las operaciones de base de datos relacionadas
// con productos en MongoDB.
//
// Al extender MongoRepository heredamos gratis:
// save(), findById(), findAll(), deleteById(), count()
// ============================================================

package com.modex.productservice.repository;

import com.modex.productservice.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository
        extends MongoRepository<Product, String> {

    // Busca todos los productos activos
    // Se usa en el catalogo principal de la tienda
    List<Product> findByActiveTrue();

    // Busca productos activos por categoria
    // Ejemplo: findByCategoryAndActiveTrue("CAMISETA")
    // Se usa cuando el usuario filtra por tipo de prenda
    List<Product> findByCategoryAndActiveTrue(String category);

    // Busca productos activos por marca
    // Ejemplo: findByBrandAndActiveTrue("Zara")
    List<Product> findByBrandAndActiveTrue(String brand);

    // Busca productos activos por categoria y marca
    // Se usa cuando el usuario filtra por los dos al mismo tiempo
    List<Product> findByCategoryAndBrandAndActiveTrue(
            String category, String brand);

    // Busca productos que tengan una talla especifica disponible
    // @Query es una anotacion que permite escribir queries
    // de MongoDB directamente cuando el query derivado
    // no es suficiente para lo que necesitas.
    // Este query busca productos donde el array availableSizes
    // contiene la talla especificada.
    @Query("{ 'available_sizes': ?0, 'active': true }")
    List<Product> findByAvailableSizesContaining(String size);

    // Verifica si existe un producto con ese nombre
    // Se usa para evitar productos duplicados
    boolean existsByNameAndActiveTrue(String name);
}