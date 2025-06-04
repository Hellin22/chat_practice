package practice.chatstomp.stomp.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.*;
import practice.chatstomp.stomp.dto.ChatMessageDto;
import practice.chatstomp.stomp.service.ChatMessageService;

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

    // Websocket 으로 부터 넘어오는 메시지 처리
    @MessageMapping("{roomId}")
    // @DestonationVariable은 MessageMapping에서 전송되는 URL에서 roomId를 뺴오는 역할을 한다. (@GetMapping - @Pathvariable과 동일)
    public void sendMessage(@DestinationVariable String roomId, ChatMessageDto chatMessageDto
                            // Websocket 세션 정보를 관리하는 객체 ( 주로 사용자 인증 정보 or 세션 데이터)
                            // 서버 측에서 Websocket 세션을 통해 자동으로 관리하는 객체로 request시 특정 값을 넣어줄 필요 X
            , SimpMessageHeaderAccessor simpMessageHeaderAccessor){

        if (ChatMessageDto.MessageType.ENTER.equals(chatMessageDto.getType())) {
            // 새로 들어온 클라이언트이기 때문에, Websocket의 세션에 클라이언드의 이름과 채팅방 번호를 저장한다.

            // 이 부분은 굳이 필요한지 모르겠다. 혹시나 모를 예외 상황을 위해서 처리하는 것인가? (ENTER의 경우는 클라이언트가 보내는게 아니어서)
            simpMessageHeaderAccessor.getSessionAttributes().put("username", chatMessageDto.getSender());
            simpMessageHeaderAccessor.getSessionAttributes().put("roomId",chatMessageDto.getRoomId());
            chatMessageDto.setMessage(chatMessageDto.getSender() + "님이 입장하셨습니다.");
        }

        chatMessageService.sendMessage(roomId, chatMessageDto);
    }

//    private String roomId;
//    private String sender;
//    private String message;
//    private ChatMessageDto.MessageType type;
//
//    public enum MessageType {
//        ENTER, CHAT, LEAVE
//    }
    @GetMapping("/{roomId}/message") // 채팅방 접속시에 해당 채팅방에 있는 채팅 메시지 가져오기
    public List<ChatMessageDto> getRoomMessages(@PathVariable String roomId, @RequestParam String memberId) {

        List<ChatMessageDto> chatMessageDtoList = chatMessageService.getRoomMessages(roomId, memberId);

        return chatMessageDtoList;
    }

    // 채팅방을 처음 들어왔는지, 기존에 있었는지 확인하는 메소드 ( chatMessageController의 getRoomMessages에 사용 )
    // 나갔다가 다시 들어온 사람의 경우 주로 사용 -> 회원 정보는 DB에 있지만, 없었을때 메시지를 모두 보여주면 X
    @GetMapping("/{roomId}/isFirstJoin")
    public Boolean isFirstJoin(@PathVariable String roomId, @RequestParam String memberId){
        return chatMessageService.isFirstJoin(roomId, memberId);
    }












}
