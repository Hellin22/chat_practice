<template>
  <div class="login">
    <h2>회원 번호 입력</h2>
    <input v-model="memberId" placeholder="회원 번호를 입력하세요" />
    <input v-model="username" placeholder="이름 (선택)" />
    <button @click="login">입장</button>
    <button @click="healthCheck">건강체크</button>
    
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const memberId = ref('')
const username = ref('')
const router = useRouter()

const healthCheck = async () => {
  try {
    const response = await axios.get('http://localhost/health') // 백엔드가 /health 엔드포인트를 제공해야 함
    console.log('✅ 서버 상태:', response.data)
  } catch (error) {
    console.error('❌ 서버 상태 확인 실패:', error)
  }
}

const login = () => {
  if (!memberId.value) {
    alert('회원 번호는 필수입니다.')
    return
  }

  sessionStorage.setItem('memberId', memberId.value)
  sessionStorage.setItem('username', username.value || `사용자${memberId.value}`)

  router.push('/chat')
}
</script>
