// ============================================================
// Product.java - Model del producto en Modex
// Representa una prenda de ropa en MongoDB.
// Implementa Serializable para poder guardarse en Redis.
//
// Un producto en Modex es una prenda especifica con sus
// atributos — nombre, precio, categoria, tallas y colores.
// ============================================================

package com.modex.productservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Serializable - necesario para que Redis pueda guardar
// este objeto en cache. Sin esto Redis no puede serializar
// el producto y lanza error al intentar guardarlo.
@Document(collection = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product implements Serializable {

    // serialVersionUID - identificador de version para
    // la serializacion. Si cambias la clase y este numero
    // no coincide Redis no puede deserializar objetos viejos.
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    // Nombre del producto — indexado para busqueda de texto
    // @TextIndexed permite buscar productos por nombre con
    // busqueda de texto completo en MongoDB.
    // Ejemplo: buscar "camiseta" encuentra "Camiseta Negra M"
    @TextIndexed
    @Field("name")
    private String name;

    // Descripcion detallada del producto
    @TextIndexed
    @Field("description")
    private String description;

    // Precio — usamos BigDecimal en vez de Double para
    // dinero porque Double tiene errores de precision.
    // Ejemplo: 0.1 + 0.2 en Double = 0.30000000000000004
    // Con BigDecimal: 0.1 + 0.2 = 0.3 exacto.
    @Field("price")
    private BigDecimal price;

    // Categoria de la prenda
    // Ejemplos: CAMISETA, PANTALON, ZAPATOS, ACCESORIO,
    // VESTIDO, CHAQUETA, FALDA
    @Indexed
    @Field("category")
    private String category;

    // Marca del producto
    // Ejemplos: Nike, Zara, H&M, Adidas
    @Indexed
    @Field("brand")
    private String brand;

    // Lista de tallas disponibles para este producto.
    // Usamos List<String> porque un producto puede tener
    // multiples tallas disponibles al mismo tiempo.
    // Ejemplos: ["S", "M", "L", "XL"] o ["38", "39", "40"]
    @Field("available_sizes")
    private List<String> availableSizes;

    // Lista de colores disponibles.
    // Ejemplos: ["Negro", "Blanco", "Rojo"]
    @Field("available_colors")
    private List<String> availableColors;

    // URL de la imagen principal del producto.
    // En produccion usarias un servicio como S3 de Amazon
    // para guardar las imagenes. Por ahora guardamos la URL.
    @Field("image_url")
    private String imageUrl;

    // Stock total disponible del producto.
    // Cuando alguien hace un pedido el Inventory Service
    // reduce este numero a traves de Kafka.
    @Field("stock")
    private Integer stock;

    // Si el producto esta activo y visible en el catalogo.
    // Soft delete — en vez de borrar el producto lo desactivamos.
    @Builder.Default
    @Field("active")
    private Boolean active = true;

    // Fechas automaticas
    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;
}