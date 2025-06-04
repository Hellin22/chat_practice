package com.song.chatpractice.websocket.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.song.chatpractice.websocket.dto.ChatRoomDTO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ObjectMapper objectMapper;      // java -> json or json -> java 해주는 역할

    // key값인 String은 roomId, value값으로 chatRoomDTO가 들어간다. - 여러 채팅방들의 정보를 저장하는 Map이다.
    private Map<String, ChatRoomDTO> chatRooms;

    @Autowired
    public ChatServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // Bean 생성과 의존성 주입을 모두 완료한 뒤에 실행되는 메소드 (안전한 초기화 보장, 1번만 실행됨을 보장, 동시성 문제 예방)
    @PostConstruct      // @PostConstruct는 의존성 주입이 모두 완료된 후 초기화를 수행하는 메소드
    private void init(){
        chatRooms = new LinkedHashMap<>();
    }

    // 채팅방 생성 메소드
    @Override
    public ChatRoomDTO createRoom(String name) {
        // 채팅방 번호는 랜덤으로 배정
        String randomRoomId = UUID.randomUUID().toString();

        ChatRoomDTO chatRoom = ChatRoomDTO.builder()
                .roomId(randomRoomId)
                .name(name)
                .build();

        // 새로운 채팅방이 개설되면 chatRooms(모든 채팅방 정보를 가진 Map)에 추가해줌
        chatRooms.put(randomRoomId, chatRoom);
        return chatRoom;
    }


    // 모든 채팅방을 조회하는 메소드
    @Override
    public List<ChatRoomDTO> findAllRooms(){
        return new ArrayList<>(chatRooms.values());
    }

    // roomId로 채팅방을 찾는 메소드
    @Override
    public ChatRoomDTO findRoomById(String roomId) {
        // key 값으로 방을 찾아 value를 받아간다.
        return chatRooms.get(roomId);
    }

    // 메세지의 type이 TALK일 경우 메세지를 보내는 메소드
    @Override
    public <T> void sendMessage(WebSocketSession session, T message) {
        try{
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch(IOException e){
            log.error(e.getMessage(), e);
        }
    }
}
