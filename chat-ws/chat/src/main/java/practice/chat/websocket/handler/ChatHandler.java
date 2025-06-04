package practice.chat.websocket.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatHandler extends TextWebSocketHandler {
//    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final Map<String, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();


    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String path = session.getUri().getPath(); // "/ws/chat/1"
        String[] parts = path.split("/");
        String roomId = parts[parts.length - 1];

        roomSessions.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet())
                .add(session);
        session.getAttributes().put("roomId", roomId);

        System.out.println("==== 클라이언트 접속됨 ====");
        System.out.println("Session ID: " + session.getId());
        System.out.println("Remote IP: " + session.getRemoteAddress());
        System.out.println("URI: " + session.getUri());
        System.out.println("Headers: " + session.getHandshakeHeaders());
        System.out.println("==== 현재 세션 수: " + roomSessions.size() + " ====");
        for(WebSocketSession session2 : roomSessions.get(roomId)){
            System.out.println("Session ID: " + session2.getId());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String roomId = (String) session.getAttributes().get("roomId");
        Set<WebSocketSession> sessionsInRoom = roomSessions.getOrDefault(roomId, Set.of());

        System.out.println("==== 메시지 수신 ====");
        System.out.println("From Session ID: " + session.getId());
        System.out.println("Message: " + message.getPayload());

        for (WebSocketSession s : sessionsInRoom) {
            if (s.isOpen()) {
                s.sendMessage(message);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        System.out.println("==== 클라이언트 접속 종료한다!!!!!! ====");
        System.out.println("접속 종료 이유: " + status.getReason() + " " + status.getCode());
        System.out.println("Session ID: " + session.getId());
        System.out.println("Remote IP: " + session.getRemoteAddress());
        System.out.println("URI: " + session.getUri());
        System.out.println("Headers: " + session.getHandshakeHeaders());
        System.out.println("==== 현재 세션 수: " + sessions.size() + " ====");
        sessions.remove(session);

        String roomId = (String) session.getAttributes().get("roomId");
        if (roomId != null) {
            Set<WebSocketSession> sessionsInRoom = roomSessions.get(roomId);
            if (sessionsInRoom != null) {
                sessionsInRoom.remove(session);
                if (sessionsInRoom.isEmpty()) {
                    roomSessions.remove(roomId);
                }
            }
        }
    }
}