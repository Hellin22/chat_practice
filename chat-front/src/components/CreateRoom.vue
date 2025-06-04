<template>
    <div class="modal" v-if="show" @click.self="$emit('close')">
        <div class="modal-content">
            <div class="modal-header">
                <h3>새 채팅방 만들기</h3>
                <button class="close-btn" @click="$emit('close')">&times;</button>
            </div>
            <div class="input-group">
                <label>채팅방 이adsfsdf름</label>
                <input 
                    v-model="roomName" 
                    placeholder="채팅방 이름을 입력하세요" 
                    class="room-name-input"
                />
            </div>
            <div class="input-group">
                <label>참여자 선택</label>
                <div class="search-box">
                    <input 
                        type="text" 
                        v-model="searchTerm" 
                        placeholder="참여자 검색..."
                        class="search-input"
                    />
                </div>
                <div class="user-list">
                    <div v-for="user in filteredUsers" 
                        :key="user.userId" 
                        class="user-item"
                        :class="{ 'selected': selectedUsers.includes(user.userId) }">
                        <input 
                            type="checkbox" 
                            :id="user.userId"
                            :value="user.userId"
                            v-model="selectedUsers"
                        >
                        <label :for="user.userId">
                            <span class="user-name">{{ user.username }}</span>
                            <span class="user-id">({{ user.userId }})</span>
                        </label>
                    </div>
                </div>
            </div>
            <div class="button-group">
                <button @click="createRoom" :disabled="!isValid" class="create-btn">
                    채팅방 만들기
                </button>
                <button @click="$emit('close')" class="cancel-btn">취소</button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

const props = defineProps({
    show: Boolean,
    currentUserId: String
})

const emit = defineEmits(['close', 'roomCreated'])

const roomName = ref('')
const users = ref([])
const selectedUsers = ref([])
const searchTerm = ref('')

const filteredUsers = computed(() => {
    return users.value.filter(user => {
        const searchLower = searchTerm.value.toLowerCase()
        return user.username.toLowerCase().includes(searchLower) ||
            user.userId.toLowerCase().includes(searchLower)
    })
})

const isValid = computed(() => {
    return roomName.value.trim() && selectedUsers.value.length > 0
})

const loadUsers = async () => {
    try {
        const response = await axios.get('http://localhost:8080/member')
        console.log("response:", props.currentUserId) // 데이터 출력 확인
        users.value = response.data.filter(user => user.userId !== props.currentUserId)
        console.log("filtered users:", users.value) // 필터링된 데이터 확인
    } catch (error) {
        console.error('사용자 목록 로딩 실패:', error)
    }
}

const createRoom = async () => {
    try {
        console.log("씨발")
        // 현재 사용자도 참여자 목록에 추가
        const participants = [...selectedUsers.value, props.currentUserId]

        console.log(participants)

        // ChatRoomDto 형식에 맞게 데이터 구성
        const chatRoomDto = {
            name: roomName.value,
            participants: participants
        }
        
        const response = await axios.post('http://localhost:8080/stomp/chatRoom/create', chatRoomDto)
        
        emit('roomCreated', response.data)
        emit('close')
        
        // 입력값 초기화
        roomName.value = ''
        selectedUsers.value = []
    } catch (error) {
        console.error('채팅방 생성 실패:', error)
    }
}

onMounted(() => {
    console.log("뭐지")
    loadUsers()
})
</script>

<style scoped>
.modal {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
    backdrop-filter: blur(3px);
}

.modal-content {
    background: white;
    padding: 0;
    border-radius: 12px;
    width: 90%;
    max-width: 500px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.modal-header {
    padding: 1.5rem;
    border-bottom: 1px solid #eee;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.modal-header h3 {
    margin: 0;
    color: #333;
    font-size: 1.25rem;
}

.close-btn {
    background: none;
    border: none;
    font-size: 1.5rem;
    cursor: pointer;
    color: #666;
    padding: 0 0.5rem;
}

.input-group {
    padding: 1.5rem;
    border-bottom: 1px solid #eee;
}

.input-group label {
    display: block;
    margin-bottom: 0.75rem;
    font-weight: 600;
    color: #333;
    font-size: 0.95rem;
}

.room-name-input {
    width: 100%;
    padding: 0.75rem 1rem;
    border: 1px solid #ddd;
    border-radius: 8px;
    font-size: 1rem;
    transition: border-color 0.2s;
}

.room-name-input:focus {
    border-color: #ffeb33;
    outline: none;
}

.search-box {
    margin-bottom: 1rem;
}

.search-input {
    width: 100%;
    padding: 0.75rem 1rem;
    border: 1px solid #ddd;
    border-radius: 8px;
    font-size: 0.9rem;
}

.user-list {
    max-height: 200px;
    overflow-y: auto;
    border: 1px solid #eee;
    border-radius: 8px;
    background: #f8f9fa;
}

.user-item {
    padding: 0.75rem 1rem;
    display: flex;
    align-items: center;
    gap: 0.75rem;
    transition: background-color 0.2s;
}

.user-item:hover {
    background-color: #f0f0f0;
}

.user-item.selected {
    background-color: #fff9e6;
}

.user-item label {
    margin: 0;
    cursor: pointer;
    flex: 1;
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.user-name {
    font-weight: 500;
}

.user-id {
    color: #666;
    font-size: 0.9em;
}

.button-group {
    padding: 1.5rem;
    display: flex;
    gap: 1rem;
}

.create-btn, .cancel-btn {
    flex: 1;
    padding: 0.875rem;
    border: none;
    border-radius: 8px;
    font-size: 0.95rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
}

.create-btn {
    background-color: #ffeb33;
}

.create-btn:hover:not(:disabled) {
    background-color: #ffd700;
}

.create-btn:disabled {
    background-color: #ddd;
    cursor: not-allowed;
}

.cancel-btn {
    background-color: #f5f5f5;
    color: #666;
}

.cancel-btn:hover {
    background-color: #eee;
}

/* 스크롤바 스타일링 */
.user-list::-webkit-scrollbar {
    width: 6px;
}

.user-list::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 3px;
}

.user-list::-webkit-scrollbar-thumb {
    background: #ddd;
    border-radius: 3px;
}

.user-list::-webkit-scrollbar-thumb:hover {
    background: #ccc;
}
</style> 