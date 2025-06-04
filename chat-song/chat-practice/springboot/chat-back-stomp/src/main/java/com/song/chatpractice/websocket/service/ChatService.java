package com.song.chatpractice.websocket.service;

import com.song.chatpractice.websocket.dto.ChatRoomDTO;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

public interface ChatService {
    <T> void sendMessage(WebSocketSession session, T message);

    // 채팅방 생성 메소드
    ChatRoomDTO createRoom(String name);

    // 모든 채팅방을 조회하는 메소드
    List<ChatRoomDTO> findAllRooms();

    ChatRoomDTO findRoomById(String roomId);
}
