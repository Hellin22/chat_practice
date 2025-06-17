package practice.chatstomp.stomp.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import practice.chatstomp.stomp.entity.ChatRoom;


@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {
}