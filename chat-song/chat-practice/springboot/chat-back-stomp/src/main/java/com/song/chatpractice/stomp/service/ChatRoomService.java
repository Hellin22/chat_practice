package com.song.chatpractice.stomp.service;

import com.song.chatpractice.stomp.dto.ChatRoomDto;
import com.song.chatpractice.stomp.entity.ChatRoom;

import java.util.List;

public interface ChatRoomService {

    List<ChatRoomDto> getAllRooms();

    ChatRoomDto createChatRoom(ChatRoomDto chatRoomDto);

    ChatRoom leaveChatRoom(String roomId, String memberId);
}
