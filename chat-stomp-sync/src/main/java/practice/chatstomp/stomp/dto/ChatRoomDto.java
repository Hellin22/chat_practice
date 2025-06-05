package practice.chatstomp.stomp.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ChatRoomDto {

    private String id;
    private String name;
    private List<String> participants;
}