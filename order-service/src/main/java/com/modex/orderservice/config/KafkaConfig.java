// ============================================================
// KafkaConfig.java - Configuracion de Kafka
// Configura los topics de Kafka que usa el Order Service.
// Crea los topics automaticamente si no existen.
// ============================================================

package com.modex.orderservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    // Topic donde el Order Service publica eventos de pedidos creados.
    // El Inventory Service escucha este topic.
    // 1 particion y 1 replica — suficiente para desarrollo.
    @Bean
    public NewTopic orderCreatedTopic() {
        return TopicBuilder.name("order.created")
                .partitions(1)
                .replicas(1)
                .build();
    }

    // Topic donde el Inventory Service publica cuando
    // reservo el stock exitosamente.
    // El Order Service escucha este topic para confirmar pedidos.
    @Bean
    public NewTopic stockReservedTopic() {
        return TopicBuilder.name("inventory.stock.reserved")
                .partitions(1)
                .replicas(1)
                .build();
    }

    // Topic donde el Inventory Service publica cuando
    // no hay stock disponible.
    // El Order Service escucha este topic para cancelar pedidos.
    @Bean
    public NewTopic stockFailedTopic() {
        return TopicBuilder.name("inventory.stock.failed")
                .partitions(1)
                .replicas(1)
                .build();
    }
}