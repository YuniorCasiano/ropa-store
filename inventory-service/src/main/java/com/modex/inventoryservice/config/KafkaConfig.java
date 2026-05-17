package com.modex.inventoryservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    // Topic donde publicamos cuando el stock fue reservado
    @Bean
    public NewTopic stockReservedTopic() {
        return TopicBuilder.name("inventory.stock.reserved")
                .partitions(1)
                .replicas(1)
                .build();
    }

    // Topic donde publicamos cuando no hay stock disponible
    @Bean
    public NewTopic stockFailedTopic() {
        return TopicBuilder.name("inventory.stock.failed")
                .partitions(1)
                .replicas(1)
                .build();
    }
}