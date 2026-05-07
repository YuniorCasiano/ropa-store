// ============================================================
// MongoConfig.java - Configuracion de MongoDB
// Activa las fechas automaticas @CreatedDate y
// @LastModifiedDate en los documentos de MongoDB.
// Identico al del User Service.
// ============================================================

package com.modex.auth_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
public class MongoConfig {
    // Las dos anotaciones hacen todo el trabajo.
    // @Configuration — Spring lee esta clase al arrancar.
    // @EnableMongoAuditing — activa las fechas automaticas.
}