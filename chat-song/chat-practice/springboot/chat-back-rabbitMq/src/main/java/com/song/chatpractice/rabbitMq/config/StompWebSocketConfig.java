package com.song.chatpractice.rabbitMq.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${spring.rabbitmq.port}")
    private Integer port;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/rabbitmq-chat")
                .setAllowedOrigins("http://localhost:5173")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        // RabbitMQ로 메시지를 보내 줄 경로 지정
        // /queue: point-to-point 메시징 , /topic: 발행/구독 메시징
        // /exchange: RabbitMQ의 exchange 직접 지정, /amq/queue: RabbitMQ의 특정 큐에 직접 메시징
        registry.enableStompBrokerRelay("/queue", "/topic", "/exchange", "/amq/queue")
//                .setAutoStartup(true)           // STOMP 브로커 릴레이를 자동으로 시작하도록 설정
                .setRelayHost(host)             // RabbitMQ 서버 주소
                .setRelayPort(61613)            // RabbitMQ 포트(5672), STOMP 포트(61613)
//                .setSystemLogin(username)       // RabbitMQ 시스템계정
//                .setSystemPasscode(password)    // RabbitMQ 시스템 비밀번호
                .setClientLogin(username)       // RabbitMQ 클라이언트 계정
                .setClientPasscode(password);   // RabbitMQ 클라이언트 비밀번호

        // RabbitMQ는 /가 아닌 .을 지원하기 떄문에 설정
        registry.setPathMatcher(new AntPathMatcher("."));

        // 메시지를 발행(송신 - publish)할때 사용하는 prefix 설정
        registry.setApplicationDestinationPrefixes("/app");
    }


    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(8192)
                .setSendTimeLimit(15 * 1000)
                .setSendBufferSizeLimit(3 * 512 * 1024);
    }
}
