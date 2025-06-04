package com.song.chatpractice.kafka.service;



import com.song.chatpractice.kafka.dto.ChatRoomDto;
import com.song.chatpractice.kafka.entity.ChatRoom;

import java.util.List;

public interface ChatRoomService {

    List<ChatRoomDto> getAllRooms();

    ChatRoomDto createChatRoom(ChatRoomDto chatRoomDto);

    ChatRoom leaveChatRoom(String roomId, String memberId);
}
