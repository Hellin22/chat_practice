package practice.chatstomp.stomp.repository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import practice.chatstomp.stomp.entity.ChatMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
//    List<ChatMessage> findAllMsgByRoomId(String roomId);

    Optional<ChatMessage> findTopByRoomIdAndSenderAndTypeOrderBySendDateDesc(String roomId, String memberId,
                                                                             ChatMessage.MessageType messageType);

    List<ChatMessage> findByRoomIdAndSendDateAfterOrderBySendDateAsc(String roomId, LocalDateTime leaveTime);

    List<ChatMessage> findByRoomIdOrderBySendDateAsc(String roomId);
}