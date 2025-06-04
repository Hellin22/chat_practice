package com.song.chatpractice.kafka.config;

import com.song.chatpractice.kafka.dto.ChatMessageDto;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaProducerConfig {

    private final KafkaProperties kafkaProperties;

    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String prod_bootstrap_server;

    // ack 설정
    private final String[] acks = {"0", "1", "all"};

    @Autowired
    public KafkaProducerConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    // Kafka에서 메시지를 생성하는 역할을 하는 Producer 객체 생성
    @Bean
    public ProducerFactory<String, ChatMessageDto> producerFactory() {

        Map<String, Object> config = new HashMap<>();

        // 카프카 클러스터 주소 세팅 - 현재 열려있는 카프카 브로커 주소
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, prod_bootstrap_server);
        // serializer key 세팅
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // serializer value 세팅
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // Ack 세팅
        config.put(ProducerConfig.ACKS_CONFIG, acks[1]);

        return new DefaultKafkaProducerFactory<>(config);
    }

    // Kafka Producer 객체 템플릿
    @Bean
    public KafkaTemplate<String, ChatMessageDto> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

}
