package com.song.chatpractice.websocket.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageDTO {

    // 메시지 타입: 입장(ENTER), 채팅(TALK)
    private MessageType type;

    // 채팅방 번호
    private String roomId;

    // 전송자
    private String sender;

    // 메시지
    private String message;

    public enum MessageType {
        ENTER, TALK
    }
}
