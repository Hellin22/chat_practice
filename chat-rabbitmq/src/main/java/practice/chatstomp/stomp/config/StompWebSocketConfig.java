package practice.chatstomp.stomp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.config.annotation.*;

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
                // 클라이언트 ws 요청(ws 연결)을 위한 엔드포인트.
                .setAllowedOrigins("http://localhost:5173")
                .withSockJS();
                // SockJS를 통해서 ws를 지원하지 않는 브라우저도 fallback으로 연결이 가능하게 함.
                // ex) http://localhost:5173/stomp-chat 으로도 연결 가능하게함. (ws가 아닌 polling 방식 등으로)
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

//    @Override
//    public void configureMessageBroker(MessageBrokerRegistry registry) {
//
////      클라이언트가 메시지를 구독(수신 - subscribe)할 때 사용할 prefix 설정 - /queue는 1대1 , /topic은 1대다 채팅방을 의미
////      registry.enableSimpleBroker("/queue", "/topic");
//        registry.enableSimpleBroker("/topic");
//        // 쉬운 말로 설명하자면 broker의 채널을 설정하는건데 채팅 기준으로 /topic은 채팅방에 전파(1대다), /queue는 멤버에 전파(1대1)
//
//        // 메시지를 발행(송신 - publish)할때 사용하는 prefix 설정
//        registry.setApplicationDestinationPrefixes("/app");
//        // @MessageMapping의 처음에 오는 prefix를 설정.
//        // /app/roomId/1 -> 이런식으로 오면 prefix의 /app을 떼고 /roomId/{roomId}로 처리됨.
//        // 이후에는 convertAndSend로 브로커에게 메시지 브로드캐스트 요청
//        // sub가 선행되고 이게 수행되어야함.(메시지 전송 같은 것들)
//        // 이건 Spring Controller를 거침(db 저장, 인증 등)
//
//        // -> 관습상 저렇게 prefix를 구성하는거지 바꿔도 가능함.
//    }


    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(8192)
                .setSendTimeLimit(15 * 1000)
                .setSendBufferSizeLimit(3 * 512 * 1024);
    }
}