package com.song.chatpractice.kafka.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "message")
public class ChatMessage {

    @Id
    private String id;
    private String roomId;
    private String sender;
    private String message;
    private LocalDateTime sendDate;
    private MessageType type;

    public enum MessageType {
        ENTER, CHAT, LEAVE
    }
}
