# chat_practice
채팅 시스템 연습하기

1. WebSocket, Stomp(SimpleBroker)를 통해서 채팅 구현  
   -> 스프링 서버 내부 브로커이기 때문에 채팅이 많아지는 경우 다른 api 요청에 영향 가능  
   -> 백엔드 서버를 여러대로 증설하는 경우 broker pub/sub 에 대한 동기화 문제가 발생

2. WebSocket, RabbitMQ, Stomp를 통해서 채팅 구현
   -> 외부 브로커 도입하여 역할 분리
