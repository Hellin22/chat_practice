package practice.chatstomp.stomp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import practice.chatstomp.stomp.dto.ChatMessageDto;
import practice.chatstomp.stomp.entity.ChatMessage;
import practice.chatstomp.stomp.repository.ChatMessageRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("chatStompService")
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public ChatMessageService(ChatMessageRepository chatMessageRepository, SimpMessagingTemplate simpMessagingTemplate) {
        this.chatMessageRepository = chatMessageRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    // 채팅방의 모든 채팅메시지 조회
    public List<ChatMessageDto> getRoomMessages(String roomId, String memberId) {

        // 해당 user의 가장 최신 LEAVE 메시지 조회
        Optional<ChatMessage> lastLeaveMessage = chatMessageRepository
                .findTopByRoomIdAndSenderAndTypeOrderBySendDateDesc(roomId, memberId, ChatMessage.MessageType.LEAVE);

        // LEAVE 메시지가 존재한다면, LEAVE 이후의 메시지만 반환 => 향후 들어온 후 메시지만 보이게 수정할 예정
        if (lastLeaveMessage.isPresent()) {
            LocalDateTime leaveTime = lastLeaveMessage.get().getSendDate();

            // 찾은 메시지 리스트들을 Dto로 바꿔 List에 넣어서 전달
            return chatMessageRepository.findByRoomIdAndSendDateAfterOrderBySendDateAsc(roomId, leaveTime)
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }

        // LEAVE 기록이 없으면 모든 메시지 변환
        return chatMessageRepository.findByRoomIdOrderBySendDateAsc(roomId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public void sendMessage(String roomId, ChatMessageDto chatMessageDto) {

        // MongoDB에 메시지 정보 저장
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRoomId(roomId);
        chatMessage.setSender(chatMessageDto.getSender());
        chatMessage.setMessage(chatMessageDto.getMessage());
        // Type은 enum 타입임으로, 넘어오는 타입의 이름을 넣어준다.
        chatMessage.setType(ChatMessage.MessageType.valueOf(chatMessageDto.getType().name()));
        chatMessage.setSendDate(LocalDateTime.now());

        // insert() => 중복되는 key값에 대한 예외처리를 터트림
        // save() => 중복되는 key값을 update하여 덮어씌움
        chatMessageRepository.insert(chatMessage);
        // 우리는 @Id를 통해서 pk를 설정했기 때문에 중복 문제는 발생 x -> 특수한 상황을 고려해서 중복 처리 안하겠다는 의미

        // Websocket을 통해 메시지 직접 전송 - Client(front)에서는 /topic/message/방번호 를 구독(sub)하고 있는 client만 채팅을 받음
        simpMessagingTemplate.convertAndSend("/topic/message/" + roomId, chatMessageDto);
    }

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

    public ChatMessageDto leaveMessage(String roomId, String memberId, LocalDateTime leaveTime) {

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRoomId(roomId);
        chatMessage.setSender(memberId);
        chatMessage.setType(ChatMessage.MessageType.LEAVE);
        chatMessage.setSendDate(leaveTime);
        chatMessageRepository.insert(chatMessage);

        ChatMessageDto chatMessageDto = convertToDto(chatMessage);

        return chatMessageDto;
    }

    public ChatMessageDto convertToDto(ChatMessage chatMessage) {
        ChatMessageDto chatMessageDto = new ChatMessageDto();
        chatMessageDto.setRoomId(chatMessage.getRoomId());
        chatMessageDto.setSender(chatMessage.getSender());
        chatMessageDto.setMessage(chatMessage.getMessage());
        chatMessageDto.setType(ChatMessageDto.MessageType.valueOf(chatMessage.getType().name()));
        return chatMessageDto;
    }

}
