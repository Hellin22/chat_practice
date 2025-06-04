package com.song.chatpractice.kafka.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Document(collection="room")
public class ChatRoom {

    @Id
    private String id;
    private String name;
    private List<String> participants;
}
