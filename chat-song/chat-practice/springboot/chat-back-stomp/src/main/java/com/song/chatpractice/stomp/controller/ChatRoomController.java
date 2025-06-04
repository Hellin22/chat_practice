package com.song.chatpractice.stomp.controller;

import com.song.chatpractice.stomp.dto.ChatRoomDto;
import com.song.chatpractice.stomp.entity.ChatRoom;
import com.song.chatpractice.stomp.service.ChatRoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stomp/chatRoom")
@Slf4j
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @Autowired
    public ChatRoomController(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }

    // 존재하는 모든 채팅방 조회
    @GetMapping("")
    public List<ChatRoomDto> findAllRooms(){
        return chatRoomService.getAllRooms();
    }

    // 새로운 채팅방 생성
    @PostMapping("/create")
    public ChatRoomDto createChatRoom(@RequestBody ChatRoomDto chatRoomDto){
        log.info("Received name: {}", chatRoomDto.getName());
        log.info("Received participants: {}", chatRoomDto.getParticipants());
        return chatRoomService.createChatRoom(chatRoomDto);
    }

    // Client 채팅방 나가기
    @PostMapping("/{roomId}/leave")
    public ChatRoom leaveChatRoom(@PathVariable String roomId, @RequestParam String memberId){
        return chatRoomService.leaveChatRoom(roomId, memberId);
    }
}
