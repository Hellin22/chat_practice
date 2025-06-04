package com.song.chatpractice.rabbitMq.service;



import com.song.chatpractice.rabbitMq.dto.ChatRoomDto;
import com.song.chatpractice.rabbitMq.entity.ChatRoom;

import java.util.List;

public interface ChatRoomService {

    List<ChatRoomDto> getAllRooms();

    ChatRoomDto createChatRoom(ChatRoomDto chatRoomDto);

    ChatRoom leaveChatRoom(String roomId, String memberId);
}
