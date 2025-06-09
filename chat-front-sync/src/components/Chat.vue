<template>
  <div class="chat-test">
    <h2>SimpleBroker 테스트</h2>
    <div class="messages">
      <div v-for="(msg, idx) in messages" :key="idx">{{ msg.sender }}: {{ msg.content }}</div>
    </div>
    <input v-model="input" @keyup.enter="send" placeholder="메시지를 입력하세요" />
    <button @click="send">전송</button>

    <button @click="healthCheck">건강체크</button>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'
import axios from 'axios'

const input = ref('')
const messages = ref([])
const stompClient = ref(null)
const memberId = ref(sessionStorage.getItem('memberId'))
const username = ref(sessionStorage.getItem('username') || '알 수 없음')

onMounted(() => {
//   const socket = new SockJS('http://localhost:8080/ws')
  const socket = new SockJS('http://localhost/ws') // nginx 라우팅 구조로 수정(2개의 서버)
  stompClient.value = new Client({
    webSocketFactory: () => socket,
    reconnectDelay: 5000,
    onConnect: () => {
      console.log('Connected to STOMP')

      stompClient.value.subscribe('/topic/message.room1', (msg) => {
        const body = JSON.parse(msg.body)
        messages.value.push(body)
      })
    }
  })
  stompClient.value.activate()
})

onBeforeUnmount(() => {
  stompClient.value?.deactivate()
})

const healthCheck = async () => {
  try {
    const response = await axios.get('http://localhost/health') // 백엔드가 /health 엔드포인트를 제공해야 함
    const response2 = await axios.get('/health')
    console.log('✅ 서버 상태:', response.data)
    console.log('✅ 서버2 상태:', response2.data)
  } catch (error) {
    console.error('❌ 서버 상태 확인 실패:', error)
  }
}

const send = () => {
  if (!input.value || !username.value || !memberId.value) return

  const chatMessage = {
    sender: username.value,  // ← 로그인한 사용자 이름
    content: input.value,
    roomId: 'room1',
    type: 'CHAT',
    memberId: memberId.value // ← 필요시 같이 보낼 수 있음 (백엔드 로깅 등)
  }

  stompClient.value.publish({
    destination: '/app/room1',
    body: JSON.stringify(chatMessage)
  })

  input.value = ''
}
</script>

<style scoped>
.chat-test {
  max-width: 600px;
  margin: auto;
}
.messages {
  border: 1px solid #ccc;
  height: 200px;
  overflow-y: auto;
  padding: 10px;
  margin-bottom: 10px;
}
</style>
