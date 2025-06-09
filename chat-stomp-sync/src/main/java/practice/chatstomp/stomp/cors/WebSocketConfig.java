package practice.chatstomp.stomp.cors;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import practice.chatstomp.stomp.handler.CustomHandshakeInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // 클라이언트 연결 경로
                .addInterceptors(new CustomHandshakeInterceptor()) // 커스텀 인터셉터
                .setAllowedOriginPatterns("*")
                .withSockJS(); // sockJS fallback 허용
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic"); // 메시지 브로커 구독 prefix
        registry.setApplicationDestinationPrefixes("/app"); // 클라이언트 → 서버 메시지 전송 prefix
    }
}
