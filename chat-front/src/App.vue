<script setup>
import { ref, onMounted } from 'vue';

const ws = ref(null);
const input = ref('');
const messages = ref([]);

const connect = () => {
  ws.value = new WebSocket("ws://localhost:8080/ws/chat");

  ws.value.onmessage = (event) => {
    messages.value.push(event.data);
  };
};

const send = () => {
  if (ws.value && ws.value.readyState === WebSocket.OPEN) {
    ws.value.send(input.value);
    input.value = '';
  }
};

onMounted(() => {
  connect();
});
</script>

<template>
  <div>
    <input v-model="input" @keyup.enter="send" placeholder="메시지 입력" />
    <button @click="send">전송</button>
    <ul>
      <li v-for="(msg, index) in messages" :key="index">{{ msg }}</li>
    </ul>
  </div>
</template>