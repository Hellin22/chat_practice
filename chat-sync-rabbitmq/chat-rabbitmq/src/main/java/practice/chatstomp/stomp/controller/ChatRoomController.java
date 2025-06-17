package practice.chatstomp.stomp.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import practice.chatstomp.stomp.dto.ChatRoomDto;
import practice.chatstomp.stomp.entity.ChatRoom;
import practice.chatstomp.stomp.service.ChatRoomService;

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

//    private String id;
//    private String name;
//    private List<String> participants;
    // 존재하는 모든 채팅방 조회
    @GetMapping("")
    public List<ChatRoomDto> findAllRooms() {
        return chatRoomService.findAllRooms();
    }

    // 새로운 채팅방 생성
    /*
    * 채팅방 이름
    * 유저id 필요
    * */

    // 새로운 채팅방 생성
    @PostMapping("/create")
    public ChatRoomDto createChatRoom(@RequestBody ChatRoomDto chatRoomDto){
        log.info("Received name: {}", chatRoomDto.getName());
        log.info("Received participants: {}", chatRoomDto.getParticipants());
        return chatRoomService.createChatRoom(chatRoomDto);
    }

    // 내가 속한 모든 채팅방 가져오는 로직 필요


    // Client 채팅방 나가기
    @PostMapping("/{roomId}/leave")
    public ChatRoom leaveChatRoom(@PathVariable String roomId, @RequestParam String memberId){
        return chatRoomService.leaveChatRoom(roomId, memberId);
    }

}
