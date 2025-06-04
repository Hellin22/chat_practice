package com.song.chatpractice.websocket.dto;

import com.song.chatpractice.websocket.service.ChatService;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashSet;
import java.util.Set;

@Getter
public class ChatRoomDTO {

    // 채팅방 번호
    private String roomId;

    // 채팅방 이름
    private String name;

    // 입장한 클라이언트의 정보 ( WebsocketSession 정보 list ) => HashSet을 이용하여 중복 세션 추가를 방지한다.
    private Set<WebSocketSession> sessions = new HashSet<>();

    // @Setter대신 Builder 패턴 적용하여 불변성 유지
    @Builder
    public ChatRoomDTO(String roomId, String name) {
        this.roomId = roomId;
        this.name = name;
    }

    // 클라이언트의 action(ENTER, TALK) 처리 => roomId를 서비스로 부터 조회하여 json에 담긴 메시지를 전달해주는 메소드
    public void handleActions(WebSocketSession session, ChatMessageDTO chatMessageDTO, ChatService chatService){
        if (chatMessageDTO.getType().equals(ChatMessageDTO.MessageType.ENTER)){
            // 세션에 연결
            sessions.add(session);
            chatMessageDTO.setMessage(chatMessageDTO.getSender() + "님이 입장했습니다.");
        }
        sendMessage(chatMessageDTO, chatService);
    }

    // 채팅방에 존재하는 모든 클라이언트들에게 해당 메시지 전송
    public <T> void sendMessage(T message, ChatService chatService) {
        // parallelStream()은 여러 스레드를 사용해 데이터를 병렬로 처리한다.( stream보다 빠름 )
        sessions.parallelStream().forEach(session -> chatService.sendMessage(session, message));
    }
}
