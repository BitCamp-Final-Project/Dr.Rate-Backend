package com.bitcamp.drrate.global.config;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;


// Kafka 토픽 생성에 필요한 설정을 추가
@Configuration
public class KafkaConfig {
    @Bean
    public KafkaAdmin kafkaAdmin() {
        return new KafkaAdmin(Map.of(
                "bootstrap.servers", "localhost:9092"
        ));
    }
}
