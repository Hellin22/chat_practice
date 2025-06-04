package com.song.chatpractice.websocket.controller;

import com.song.chatpractice.websocket.dto.ChatRoomDTO;
import com.song.chatpractice.websocket.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("chatWebsocketController")
@RequestMapping("/chat/websocket")
public class ChatController {

    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("")
    public ChatRoomDTO createRoom(@RequestBody String name){

        return chatService.createRoom(name);
    }

    @GetMapping("")
    public List<ChatRoomDTO> findAllRoom(){
        return chatService.findAllRooms();
    }
}
