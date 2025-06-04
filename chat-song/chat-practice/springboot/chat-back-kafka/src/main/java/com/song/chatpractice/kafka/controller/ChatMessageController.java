package com.song.chatpractice.kafka.controller;

import com.song.chatpractice.kafka.dto.ChatMessageDto;
import com.song.chatpractice.kafka.service.ChatMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("chatStompController")
@RequestMapping("/stomp/chat")
@Slf4j
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    @Autowired
    public ChatMessageController(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    @MessageMapping("{roomId}")
    // @DestonationVariable은 MessageMapping에서 전송되는 URL에서 roomId를 뺴오는 역할을 한다. (@GetMapping - @Pathvariable과 동일)
    public void sendMessage(@DestinationVariable String roomId, ChatMessageDto chatMessageDto
                            // Websocket 세션 정보를 관리하는 객체 ( 주로 사용자 인증 정보 or 세션 데이터)
                            // 서버 측에서 Websocket 세션을 통해 자동으로 관리하는 객체로 request시 특정 값을 넣어줄 필요 X
                            ,SimpMessageHeaderAccessor simpMessageHeaderAccessor){

        if (ChatMessageDto.MessageType.ENTER.equals(chatMessageDto.getType())) {
            // 새로 들어온 클라이언트이기 때문에, Websocket의 세션에 클라이언드의 이름과 채팅방 번호를 저장한다.
            simpMessageHeaderAccessor.getSessionAttributes().put("username", chatMessageDto.getSender());
            simpMessageHeaderAccessor.getSessionAttributes().put("roomId",chatMessageDto.getRoomId());
            chatMessageDto.setMessage(chatMessageDto.getSender() + "님이 입장하셨습니다.");
        }

        chatMessageService.sendMessage(roomId, chatMessageDto);
    }

    // 채팅방의 채팅 메시지 가져오기 ( 채팅방 들어갔을 때, 이전 대화 목록 화면에 뿌리기 )
    @GetMapping("/{roomId}/message")
    public List<ChatMessageDto> getRoomMessages(@PathVariable String roomId, @RequestParam String memberId){

        return chatMessageService.getRoomMessages(roomId, memberId);
    }

    // 채팅방을 처음 들어왔는지, 기존에 있었는지 확인하는 메소드 ( chatMessageController의 getRoomMessages에 사용 )
    // 나갔다가 다시 들어온 사람의 경우 주로 사용 -> 회원 정보는 DB에 있지만, 없었을때 메시지를 모두 보여주면 X
    @GetMapping("/{roomId}/isFirstJoin")
    public Boolean isFirstJoin(@PathVariable String roomId, @RequestParam String memberId){
        return chatMessageService.isFirstJoin(roomId, memberId);
    }
}
