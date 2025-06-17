package practice.chatstomp.stomp.entity;

import lombok.*;
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