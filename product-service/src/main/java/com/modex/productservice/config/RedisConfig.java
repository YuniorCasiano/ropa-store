// ============================================================
// RedisConfig.java - Configuracion de Redis
// Configura el serializador de Redis para que guarde
// los objetos Java como JSON en vez de bytes binarios.
// Esto hace el cache mas legible y compatible.
// ============================================================

package com.modex.productservice.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

// @EnableCaching activa el sistema de cache de Spring.
// Sin esta anotacion las anotaciones @Cacheable,
// @CacheEvict y @CachePut no funcionan.
@EnableCaching
@Configuration
public class RedisConfig {

    // @Bean - Spring crea y administra este RedisCacheManager.
    // El CacheManager es el componente central del sistema
    // de cache — gestiona todos los caches de la aplicacion.
    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory) {

        // ObjectMapper convierte objetos Java a JSON y viceversa.
        // Necesitamos configurarlo especialmente para Redis.
        ObjectMapper objectMapper = new ObjectMapper();

        // JavaTimeModule permite serializar LocalDateTime
        // y otros tipos de fecha de Java 8+.
        // Sin esto Redis no puede guardar las fechas del producto.
        objectMapper.registerModule(new JavaTimeModule());

        // activateDefaultTyping agrega informacion del tipo
        // de clase en el JSON guardado en Redis.
        // Esto permite que Redis sepa a que clase Java
        // deserializar cuando lee el cache.
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        // GenericJackson2JsonRedisSerializer usa Jackson
        // para convertir objetos Java a JSON al guardar en Redis
        // y de JSON a objetos Java al leer del cache.
        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        // RedisCacheConfiguration define como se comporta el cache
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration
                .defaultCacheConfig()
                // TTL - Time To Live: 1 hora
                // Despues de 1 hora Redis elimina el cache automaticamente
                .entryTtl(Duration.ofHours(1))
                // No cachear valores null — si un producto no existe
                // no guardamos null en cache
                .disableCachingNullValues()
                // Serializador para las claves del cache — usa String
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                )
                // Serializador para los valores del cache — usa JSON
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(serializer)
                );

        // Construimos el CacheManager con la configuracion
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfig)
                .build();
    }
}