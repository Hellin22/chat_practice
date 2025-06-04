package com.song.chatpractice.websocket.config;

import com.song.chatpractice.websocket.handler.WebSocketChatHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

/* 설명.
    Websocket Config 설정
    WebsocketChatHandler를 이용하여 websocket을 활성화하기 위한 config 파일 작성
    endpoint 작성 => /ws/chat
    cors 처리 => setAllowedOrigins("*")
 */

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketChatHandler webSocketChatHandler;

    @Autowired
    public WebSocketConfig(WebSocketChatHandler webSocketChatHandler) {
        this.webSocketChatHandler = webSocketChatHandler;
    }

    // Client와 Server를 연결할 Handler 등록
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        // "ws/chat"로 endpoint를 설정한다. (클라이언트가 연결 생성시, ws://localhost:8080/ws/chat으로 요청을 보내야함)
        // setAllowedOrigins("*") => 모든 도메인으로 부터의 접근 허용 ( CORS )
        registry.addHandler(webSocketChatHandler, "ws/chat").setAllowedOrigins("*");
    }
}
