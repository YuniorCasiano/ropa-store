// ============================================================
// MongoConfig.java — Configuración de MongoDB
// Este archivo tiene dos responsabilidades:
// 1. Activar el sistema de auditoría de MongoDB para que
//    @CreatedDate y @LastModifiedDate funcionen automáticamente.
// 2. Configurar el MongoTemplate para operaciones avanzadas
//    que el Repository no puede hacer directamente.
// ============================================================

package com.ropastore.user_service.config;

// @Configuration le dice a Spring que esta clase contiene
// configuración especial que debe leer al arrancar.
import org.springframework.context.annotation.Configuration;

// @EnableMongoAuditing activa el sistema de auditoría de MongoDB.
// Sin esta anotación, @CreatedDate y @LastModifiedDate en el
// modelo User.java no funcionarían — las fechas quedarían null.
import org.springframework.data.mongodb.config.EnableMongoAuditing;

// @Configuration — marca esta clase como configuración de Spring.
// Spring la busca al arrancar y ejecuta todo lo que encuentre aquí.
@Configuration

// @EnableMongoAuditing — activa la auditoría automática de MongoDB.
// A partir de este momento Spring detecta los campos anotados con
// @CreatedDate y @LastModifiedDate en cualquier @Document y los
// llena automáticamente cuando guardas o actualizas documentos.
@EnableMongoAuditing
public class MongoConfig {
    // Esta clase no necesita métodos por ahora.
    // Las dos anotaciones de arriba hacen todo el trabajo.
    //
    // En el futuro podríamos agregar aquí:
    // - Configuración de índices personalizados
    // - Configuración de conversores de tipos
    // - Configuración de validadores de esquema
}
