package com.song.chatpractice.kafka.repository;

import com.song.chatpractice.kafka.entity.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

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
