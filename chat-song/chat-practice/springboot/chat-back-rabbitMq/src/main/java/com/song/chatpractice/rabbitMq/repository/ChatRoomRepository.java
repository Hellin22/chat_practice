package com.song.chatpractice.rabbitMq.repository;

import com.song.chatpractice.rabbitMq.entity.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {
}
