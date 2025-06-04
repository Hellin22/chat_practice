package com.song.chatpractice.kafka.service;


import com.song.chatpractice.kafka.dto.ChatMessageDto;
import com.song.chatpractice.kafka.entity.ChatMessage;
import com.song.chatpractice.kafka.repository.ChatMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatMessageServiceImpl implements ChatMessageService {

    @Value("${APP_NAME}")
    private String appName;

    @Value("${KAFKA_GROUP_ID}")
    private String groupId;

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final MongoTemplate mongoTemplate;
    private final KafkaTemplate<String, ChatMessageDto> kafkaTemplate;

    @Autowired
    public ChatMessageServiceImpl(ChatMessageRepository chatMessageRepository,SimpMessagingTemplate simpMessagingTemplate,
                                  MongoTemplate mongoTemplate, KafkaTemplate kafkaTemplate) {
        this.chatMessageRepository = chatMessageRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.mongoTemplate = mongoTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }

    /* 설명. Kafka Producer의 sendMessage */
    @Override
    public void sendMessage(String roomId, ChatMessageDto chatMessageDto) {

        // Kafka로 메시지 전송 (다른 서버들도 메시지를 받을 수 있도록)
        String topicName = appName;  // Kafka Topic 설정
        log.info("메시지 보낸 토픽 이름 {}: 보낸 메시지 {}", topicName, chatMessageDto);

        // ListenableFuture가 deprecated 되었음으로 대체
        CompletableFuture<SendResult<String, ChatMessageDto>> future
                // Kafka로 메시지 전송 ( 비동기 통신은 try-catch보다 completableFuture 사용)
                = kafkaTemplate.send(topicName, roomId, chatMessageDto);

        future.whenComplete((result, ex) -> {
            if (ex == null){
                log.info("메시지가 {}에 잘 전송되었습니다.", topicName);

                // 메시지 전송 성공시 DB에 저장
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setRoomId(chatMessageDto.getRoomId());
                chatMessage.setSender(chatMessageDto.getSender());
                chatMessage.setMessage(chatMessageDto.getMessage());
                chatMessage.setType(ChatMessage.MessageType.valueOf(chatMessageDto.getType().name()));
                chatMessage.setSendDate(LocalDateTime.now());
                chatMessageRepository.save(chatMessage);
                log.info("{}가 잘 저장되었습니다..", chatMessage);
            } else {
                log.error("Kafka 토픽에 메시지 전송 실패 {}: {}",topicName, chatMessageDto, ex);
            }
         });
    }

    /* 설명. Kafka Consumer의 receiveMessage */
    @KafkaListener(topics = "${APP_NAME}", groupId = "${KAFKA_GROUP_ID}")
    public void receiveMessage(ChatMessageDto chatMessageDto) {

        log.info("Received message in group {}: {}", "groupId", chatMessageDto);

        // 모든 WebSocket 서버가 Kafka 메시지를 받아서 WebSocket으로 전달!
        simpMessagingTemplate.convertAndSend("/topic/room/" + chatMessageDto.getRoomId(), chatMessageDto);
    }

    @Override
    public ChatMessageDto leaveMessage(String roomId, String memberId, LocalDateTime leaveTime) {

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRoomId(roomId);
        chatMessage.setSender(memberId);
        chatMessage.setType(ChatMessage.MessageType.LEAVE);
        chatMessage.setSendDate(leaveTime);
        chatMessageRepository.insert(chatMessage);

        ChatMessageDto chatMessageDto = convertEntityToDto(chatMessage);

        return chatMessageDto;
    }

    @Override
    public List<ChatMessageDto> getRoomMessages(String roomId, String memberId) {

        // 해당 user의 가장 최신 LEAVE 메시지 조회
        Optional<ChatMessage> lastLeaveMessage = chatMessageRepository
                .findTopByRoomIdAndSenderAndTypeOrderBySendDateDesc(roomId, memberId, ChatMessage.MessageType.LEAVE);

        // LEAVE 메시지가 존재한다면, LEAVE 이후의 메시지만 반환 => 향후 들어온 후 메시지만 보이게 수정할 예정
        if(lastLeaveMessage.isPresent()){
            LocalDateTime leaveTime = lastLeaveMessage.get().getSendDate();

            // 찾은 메시지 리스트들을 Dto로 바꿔 List에 넣어서 전달
            return chatMessageRepository.findByRoomIdAndSendDateAfterOrderBySendDateAsc(roomId, leaveTime)
                            .stream()
                            .map(this::convertEntityToDto)
                            .collect(Collectors.toList());
        }

        // LEAVE 기록이 없으면 모든 메시지 변환
        return chatMessageRepository.findByRoomIdOrderBySendDateAsc(roomId)
                .stream()
                .map(this::convertEntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Boolean isFirstJoin(String roomId, String memberId) {

        // 해당 user의 LEAVE 상태도 변경된 가장 최근의 시간 조회
        // Optional로 처리하는 이유 => Null처리가 편함
        Optional<ChatMessage> lastLeaveMessage = chatMessageRepository
                .findTopByRoomIdAndSenderAndTypeOrderBySendDateDesc(roomId, memberId, ChatMessage.MessageType.LEAVE);

        // 해당 user의 ENTER 상태도 변경된 가장 최근의 시간 조회
        Optional<ChatMessage> lastEnterMessage = chatMessageRepository
                .findTopByRoomIdAndSenderAndTypeOrderBySendDateDesc(roomId, memberId, ChatMessage.MessageType.ENTER);

        // LEAVE가 없을 경우에는 최초 입장
        if (!lastLeaveMessage.isPresent()){
            return !lastLeaveMessage.isPresent();
        }

        // LEAVE는 있는데 ENTER는 없거나(에러), LEAVE가 ENTER보다 더 최근이면, 최초 입장으로 처리 (나갔다가 다시 들어옴)
        return !lastEnterMessage.isPresent() ||
                lastLeaveMessage.get().getSendDate().isAfter(lastEnterMessage.get().getSendDate());
    }

    // ModelMapper 대신 Entity를 Dto로 변환해주는 메소드
    private ChatMessageDto convertEntityToDto(ChatMessage chatMessage){
        ChatMessageDto chatMessageDto = new ChatMessageDto();
        chatMessageDto.setRoomId(chatMessage.getRoomId());
        chatMessageDto.setSender(chatMessage.getSender());
        chatMessageDto.setMessage(chatMessage.getMessage());
        chatMessageDto.setType(ChatMessageDto.MessageType.valueOf(chatMessage.getType().name()));

        return chatMessageDto;
    }
}