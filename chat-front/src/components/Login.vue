<template>
    <div class="user-entry-container">
        <div class="entry-form">
            <h2>채팅 시작하기</h2>
            <div class="input-group">
                <input 
                    v-model="userId"
                    type="text"
                    placeholder="사용자 ID를 입력하세요"
                    @keyup.enter="handleSubmit"
                />
                <button @click="handleSubmit" :disabled="!userId">
                    확인
                </button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const userId = ref('')

const handleSubmit = async () => {
    if (userId.value) {
        try{
            const response = await axios.get(`http://localhost:8080/member/${userId.value}`)
            
            console.log("첫번째임 response", response)
            console.log("두번째임 response",response.data)

            
            localStorage.setItem('memberId', userId.value)
            localStorage.setItem('username',response.data)
        } catch (error) {
            
      if (error.response && error.response.status === 500) {
        alert('존재하지 않는 사용자입니다.') // 팝업 알림
        } else {
        alert('로그인 중 오류가 발생했습니다. 다시 시도해주세요.')
        }
        console.error('이전 메시지 로딩 실패:', error);
        }

        router.push('/chat')
    }
}
</script>

<style scoped>
.user-entry-container {
    height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: #f5f6f8;
}

.entry-form {
    background-color: white;
    padding: 2rem;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    width: 100%;
    max-width: 400px;
}

h2 {
    text-align: center;
    margin-bottom: 1.5rem;
    color: #333;
}

.input-group {
    display: flex;
    gap: 10px;
}

input {
    flex: 1;
    padding: 12px;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 1rem;
}

button {
    padding: 12px 24px;
    background-color: #ffeb33;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-weight: bold;
}

button:hover {
    background-color: #ffd700;
}

button:disabled {
    background-color: #ddd;
    cursor: not-allowed;
}
</style>