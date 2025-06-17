# chat_practice
채팅 시스템 연습하기

## 1. WebSocket, Stomp(SimpleBroker)를 통해서 채팅 구현  
   -> 스프링 서버 내부 브로커이기 때문에 채팅이 많아지는 경우 다른 api 요청에 영향 가능  
   -> 백엔드 서버를 여러대로 증설하는 경우 broker pub/sub 에 대한 동기화 문제가 발생

<br>

## 2. WebSocket, RabbitMQ, Stomp를 통해서 채팅 구현 with (chat-front, chat-rabbitmq)  
   -> 외부 브로커 도입하여 역할 분리  

### a. rabbitmq 도커 데스크탑으로 띄우기  
```
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 -p 61613:61613 --restart=unless-stopped rabbitmq:3-management
```
```
이 코드 블럭은 명령어 설명 블럭 입니다.
docker run -d 	// Detached 모드 (백그라운드 실행)
--name rabbitmq // 컨테이너 이름을 rabbitmq로 지정
-p 5672:5672 	// 컨테이너의 5672 포트를 호스트의 같은 포트에 바인딩(RMQ AMQP 기본 포트)
-p 15672:15672	// RMQ 관리 콘솔(웹 UI) 접속용 포트 -> http://localhost:15672
-p 61613:61613 	// STOMP 프로토콜 전용 포트 (Spring에서 enableStompBrokerRelay()를 쓸때 사용)
--restart=unless-stopped // 도커 재시작시 자동 재실행
rabbitmq:3-management	 // 사용하는 Docker 이미지 이름
```

<br>

### b. rabbitmq stomp plugin 설치
![image](https://github.com/user-attachments/assets/00cc7f94-4688-40cf-a588-36aeb6fc938d)
```
rabbitmq-plugins enable rabbitmq_stomp 
```
빨간색 CLI에 위 명령어를 통해서 설치

<br>

### c. 환경변수 설정  

| 환경변수 이름    | 환경변수 값  |
| ---------- | ---------------------------------------------- |
| MARIADB_PASSWORD | challengers  |
| MARIADB_URI | jdbc:mariadb://localhost:3306/chatdb  |
| MARIADB_USERNAME | challengers |
| MONGODB_URI | mongodb://localhost:27017/chatdb     |
| RMQ_HOST | localhost  |
| RMQ_PASSWORD | guest  |
| RMQ_PORT | 5672    |
| RMQ_USERNAME | guest  |

😊 마리아 디비(rdb)의 경우에는 user, db 미리 생성해놓아야 합니다.

<br>

### d. db 의존성 없이 간단하게 실습 with chat_rabbitmq, 아래 uri(프론트)
(프론트) chat-front -> https://jiangxy.github.io/websocket-debug-tool/  
(백) chat-rabbitmq에서 수정 몇가지

- chat-rabbitmq 수정
   - StompWebSocketConfig.java의 registerStompEndpoints 수정
```java
@Override
public void registerStompEndpoints(StompEndpointRegistry registry) {
   registry.addEndpoint("/rabbitmq-chat")
       //  .setAllowedOrigins("http://localhost:5173")
       .setAllowedOrigins("https://jiangxy.github.io")
       .withSockJS();
}
```

- ChatMessageController.java에 SimpMessagingTemplate 등록 및 sendMessage 수정
```java

private final ChatMessageService chatMessageService;
private final SimpMessagingTemplate simpMessagingTemplate;

@Autowired
public ChatMessageController(ChatMessageService chatMessageService, SimpMessagingTemplate simpMessagingTemplate) {
  this.chatMessageService = chatMessageService;
  this.simpMessagingTemplate = simpMessagingTemplate;
}

@MessageMapping("{roomId}")
public void sendMessage(@DestinationVariable String roomId, String message) {
   log.info("roomId: " + roomId + ", message: " + message);
   simpMessagingTemplate.convertAndSend("/topic/message." + roomId, message);
}
```

<br>

### e. rabbitmq 브로커 연동을 통한 채팅
- stomp 프론트 연동 방법  
![stomp 프론트 사용법](https://github.com/Hellin22/chat_practice/blob/main/img-gif/stomp%20%ED%94%84%EB%A1%A0%ED%8A%B8%20%EC%82%AC%EC%9A%A9%EB%B2%95.png)

😂 백엔드 먼저 켜주셔야 연결돼요!

<br>

- rabbitmq 테스트
![rabbitmq 간단 테스트](https://github.com/Hellin22/chat_practice/blob/main/img-gif/rabbitmq%20%EA%B0%84%EB%8B%A8%20%ED%85%8C%EC%8A%A4%ED%8A%B8.gif)

<br>

## 3. SimpleBroker 사용시 동기화 문제 구현  
   ![SimpleBroker 동기화 테스트](https://github.com/user-attachments/assets/f17489b4-f461-4ee7-9924-07f7cfca16a1)
   - Docker를 통해 동일한 Spring Boot 서버 2개 (app1, app2) 실행
   - Nginx를 프록시 서버로 설정해 /ws 요청을 WebSocket 연결로 프록시
   - least_conn 설정을 통해 서버에 연결된 클라이언트 수에 따라 라우팅
   - Vue 클라이언트는 localhost/ws를 통해 WebSocket 연결을 시도해 Nginx를 경유하도록 구성
   - 여러개(현재는 3개)의 클라이언트가 다른 Spring 서버에 붙도록 구성하여 SimpleBroker의 pub/sub 동기화 문제 구현
   - SimpleBroker는 Spring 서버 내 메모리 기반 브로커이므로, 서로 다른 서버간 pub/sub 동기화가 불가능
   - gif에서 한 사용자가 보낸 메시지를 다른 서버에 연결된 사용자가 받지 못하는 현상 확인 가능

<br>

## 4. RabbitMQ 사용시 동기화 문제 해결 (WITH chat-sync-rabbitmq)
   ![RabbitMQ 동기화 테스트](https://github.com/Hellin22/chat_practice/blob/main/img-gif/RabbitMQ%20%EB%8F%99%EA%B8%B0%ED%99%94%20%ED%85%8C%EC%8A%A4%ED%8A%B8.gif)  
   - 외부 메시지 브로커인 RabbitMQ를 통해 서버간 WebSocket 세션 공유 없이도 메시지 동기화 가능
   - Spring 내장 브로커인 Simple Broker에서 발생하던 세션 정보 불일치 문제를 해결해, 확장성 있는 구조로 전환
   - 서버 수를 수평확장해도 메시지 유실 없이 실시간 채팅 가능
