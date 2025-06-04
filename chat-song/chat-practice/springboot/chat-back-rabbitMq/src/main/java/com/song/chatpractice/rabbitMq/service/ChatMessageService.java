package com.song.chatpractice.rabbitMq.service;

import com.song.chatpractice.rabbitMq.dto.ChatMessageDto;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageService {

    void sendMessage(String roomId, ChatMessageDto chatMessageDto);

    ChatMessageDto leaveMessage(String roomId, String memberId, LocalDateTime leaveTime);

    List<ChatMessageDto> getRoomMessages(String roomId, String memberId);

    Boolean isFirstJoin(String roomId, String memberId);
}
