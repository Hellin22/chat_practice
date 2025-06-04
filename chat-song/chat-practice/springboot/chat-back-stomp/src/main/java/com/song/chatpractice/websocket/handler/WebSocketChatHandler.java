package com.song.chatpractice.websocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.song.chatpractice.websocket.dto.ChatMessageDTO;
import com.song.chatpractice.websocket.dto.ChatRoomDTO;
import com.song.chatpractice.websocket.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashSet;
import java.util.Set;

/* 설명.
    WebSocket Handler 추가
    Socket은 서버와 클라이언트가 1:N 관계로 연걸되기 때문에, 힌 서버에 여러 클라이언트들 접속하여 전송한 메세지를 처리해줄 Handler 필요
    TextWebSocketHandler를 상속받아서 Handler 생성
 */

@Component
@Slf4j
public class WebSocketChatHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final ChatService chatService;

    @Autowired
    public WebSocketChatHandler(ObjectMapper objectMapper, ChatService chatService) {
        this.objectMapper = objectMapper;
        this.chatService = chatService;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        // Client가 전송한 Message의 본문을 담는다. (json형태)
        String payload = message.getPayload();
        // 사용자가 입력한 문자열이 찍힘 (Ex. hello )
        log.info(payload);

        // Client와 Server가 연결되어 있는 객체를 "session"이라고 하고, 아래는 session을 통해 서버가 클라이언트에게 보낼 메시지를 담는다.
//        TextMessage textMessage = new TextMessage("Hello World!");

        // json으로 넘어오는 Payload를 message로 배분해서 넣어준다.
        ChatMessageDTO chatMessage = objectMapper.readValue(payload, ChatMessageDTO.class);

        // 넘어오는 message에 있는 roomId로 채팅방과 연결한다.
        ChatRoomDTO chatRoom = chatService.findRoomById(chatMessage.getRoomId());
        chatRoom.handleActions(session, chatMessage, chatService);
    }
}
