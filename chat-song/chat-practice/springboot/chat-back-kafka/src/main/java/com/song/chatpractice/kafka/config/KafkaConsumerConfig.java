package com.song.chatpractice.kafka.config;

import com.song.chatpractice.kafka.dto.ChatMessageDto;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    private final KafkaProperties kafkaProperties;

    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String cons_bootstrap_server;

    @Value("${spring.kafka.consumer.group-id}")
    private String cons_group_id;

    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String cons_auto_offset_reset;

    public KafkaConsumerConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    // Kafka 메시지 수신하는 Consumer 객체 생성
    @Bean
    public ConsumerFactory<String, ChatMessageDto> consumerFactory(){

        // Kafka에서 받은 메시지를 서버에 전달할 때 ChatMessageDto 객체로 변환해주는 Json 역질렬화
        JsonDeserializer<ChatMessageDto> deserializer = new JsonDeserializer<>();
        // 역질렬화 할 수 있는 패키지 지정
//        deserializer.addTrustedPackages("*");
        deserializer.addTrustedPackages("com.song.chatpractice.kafka.dto");

        Map<String,Object> props = new HashMap<>();

        // 카프카 클러스터 주소 세팅 - 현재 열려있는 카프카 브로커 주소
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, cons_bootstrap_server);
        // 카프카 Consumer group Id 세팅 - 모든 서버에 메시지 전송을 위해서는 모두 다른 아이디 설정 필요
        props.put(ConsumerConfig.GROUP_ID_CONFIG, cons_group_id);
        // 카프카 Consumer offset 세팅 ( offset을 사용할 수 없거나 정보를 찾을 수 없을 떄 option)
        // earlist - 가장 처음부터 읽기 , latest - 가장 마지막부터 읽기
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, cons_auto_offset_reset);
        // 카프카 deserializer 설정
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);

//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    // Kafka 메시지를 수신 & 처리하는 리스너 컨테이너
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ChatMessageDto> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ChatMessageDto> kafkaListenerContainerFactory
                = new ConcurrentKafkaListenerContainerFactory<>();

        kafkaListenerContainerFactory.setConsumerFactory(consumerFactory());
        return kafkaListenerContainerFactory;
    }
}
