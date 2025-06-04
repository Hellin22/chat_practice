package com.song.chatpractice.stomp.service;

import com.song.chatpractice.stomp.dto.ChatMessageDto;
import com.song.chatpractice.stomp.dto.ChatRoomDto;
import com.song.chatpractice.stomp.entity.ChatMessage;
import com.song.chatpractice.stomp.entity.ChatRoom;
import com.song.chatpractice.stomp.repository.ChatRoomRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatRoomServiceImpl implements ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public ChatRoomServiceImpl(ChatRoomRepository chatRoomRepository, ChatMessageService chatMessageService,
                               SimpMessagingTemplate simpMessagingTemplate) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageService = chatMessageService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Override
    public List<ChatRoomDto> getAllRooms() {

        // MongoDB의 jpa를 사용하여 데이터를 만들기 때문에 findAll()을 사용하고, 따로 Dto에 넣어줘야 한다.
        List<ChatRoomDto> chatRoomDtos = chatRoomRepository.findAll()
                                                        .stream()
                                                        .map(this::convertEntityToDto)
                                                        .collect(Collectors.toList());
        return chatRoomDtos;
    }

    @Override
    public ChatRoomDto createChatRoom(ChatRoomDto chatRoomDto) {

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setName(chatRoomDto.getName());
        chatRoom.setParticipants(chatRoomDto.getParticipants());

        ChatRoomDto chatRoomResponseDto = convertEntityToDto(chatRoomRepository.save(chatRoom));

        return chatRoomResponseDto;
    }

    @Override
    public ChatRoom leaveChatRoom(String roomId, String memberId) {

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        // 채팅방 참여 인원에서 나가는 사람 리스트에서 나가는 사람 삭제
        List<String> participants = new ArrayList<>(chatRoom.getParticipants());
        participants.remove(memberId);
        chatRoom.setParticipants(participants);

        // 채팅방 나가는 시점
        LocalDateTime leaveTime = LocalDateTime.now();

        // 해당 사용자가 나갔음을 채팅방에 뿌려주기 위한 Message 저장 ( 예외 처리 필요 )
        chatMessageService.leaveMessage(roomId, memberId, leaveTime);

        // 수정된 채팅방 정보 저장
        ChatRoom updateChatRoom = chatRoomRepository.save(chatRoom);

        // 채팅방에 있는 사용자들에게 업데이트 된 채팅방 정보 전송
        simpMessagingTemplate.convertAndSend("/topic/room/" + roomId + "/update", updateChatRoom);

        return updateChatRoom;
    }

    // ModelMapper 대신 Entity를 Dto로 변환해주는 메소드
    private ChatRoomDto convertEntityToDto(ChatRoom chatRoom){
        ChatRoomDto chatRoomDto = new ChatRoomDto();
        chatRoomDto.setId(chatRoom.getId());
        chatRoomDto.setName(chatRoom.getName());
        chatRoomDto.setParticipants(chatRoom.getParticipants());

        return chatRoomDto;
    }

}
