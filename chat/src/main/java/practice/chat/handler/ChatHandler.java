package practice.chat.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatHandler extends TextWebSocketHandler {
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);

        System.out.println("==== 클라이언트 접속됨 ====");
        System.out.println("Session ID: " + session.getId());
        System.out.println("Remote IP: " + session.getRemoteAddress());
        System.out.println("URI: " + session.getUri());
        System.out.println("Headers: " + session.getHandshakeHeaders());
        System.out.println("==== 현재 세션 수: " + sessions.size() + " ====");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("==== 메시지 수신 ====");
        System.out.println("From Session ID: " + session.getId());
        System.out.println("Message: " + message.getPayload());

        for (WebSocketSession s : sessions) {
            System.out.println("→ 메시지 전송 대상 Session ID: " + s.getId());
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
    }
}