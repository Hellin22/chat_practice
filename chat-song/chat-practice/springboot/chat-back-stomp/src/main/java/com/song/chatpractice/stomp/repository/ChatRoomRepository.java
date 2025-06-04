package com.song.chatpractice.stomp.repository;

import com.song.chatpractice.stomp.dto.ChatRoomDto;
import com.song.chatpractice.stomp.entity.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {
}
