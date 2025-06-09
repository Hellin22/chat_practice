package practice.chatstomp.stomp.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import practice.chatstomp.stomp.dto.ChatMessage;

@Controller
public class ChatController {

    @MessageMapping("/room1")
    @SendTo("/topic/message.room1")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        System.out.println("받은 메시지: " + chatMessage.getSender() + " → " + chatMessage.getContent());
        return chatMessage;
    }
}
