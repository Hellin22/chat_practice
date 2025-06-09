# chat_practice
채팅 시스템 연습하기

## 1. WebSocket, Stomp(SimpleBroker)를 통해서 채팅 구현  
   -> 스프링 서버 내부 브로커이기 때문에 채팅이 많아지는 경우 다른 api 요청에 영향 가능  
   -> 백엔드 서버를 여러대로 증설하는 경우 broker pub/sub 에 대한 동기화 문제가 발생

<br>

## 2. WebSocket, RabbitMQ, Stomp를 통해서 채팅 구현  
   -> 외부 브로커 도입하여 역할 분리

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
