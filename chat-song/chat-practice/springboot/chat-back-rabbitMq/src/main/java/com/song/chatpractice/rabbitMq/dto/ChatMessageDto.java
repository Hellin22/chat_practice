package com.song.chatpractice.rabbitMq.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ChatMessageDto {

    private String roomId;
    private String sender;
    private String message;
    private MessageType type;

    public enum MessageType {
        ENTER, CHAT, LEAVE
    }
}
