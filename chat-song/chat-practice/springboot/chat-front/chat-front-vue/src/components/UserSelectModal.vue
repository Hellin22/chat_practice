<template>
    <div class="modal" v-if="show" @click.self="$emit('close')">
        <div class="modal-content">
            <div class="modal-header">
                <h3>{{ isInvite ? '채팅방 초대하기' : '새 채팅방 만들기' }}</h3>
                <button class="close-btn" @click="$emit('close')">&times;</button>
            </div>
            <div class="modal-body">
                <div class="input-group" v-if="!isInvite">
                    <label>채팅방 이름</label>
                    <div class="input-wrapper">
                        <i class="icon">💬</i>
                        <input v-model="roomName" placeholder="채팅방 이름을 입력하세요" class="room-name-input" />
                    </div>
                </div>
                <div class="input-group">
                    <label>{{ isInvite ? '초대할 사용자 선택' : '참여자 선택' }}</label>
                    <div class="search-box">
                        <div class="input-wrapper">
                            <i class="icon">🔍</i>
                            <input type="text" v-model="searchTerm" placeholder="이름 또는 아이디로 검색..."
                                class="search-input" />
                        </div>
                    </div>
                    <div class="selected-count" v-if="selectedUsers.length > 0">
                        {{ selectedUsers.length }}명 선택됨
                    </div>
                    <div class="user-list">
                        <div v-for="user in filteredUsers" :key="user.userId" class="user-item" :class="{
                            'selected': selectedUsers.includes(user.userId),
                            'disabled': user.userId === props.currentUserId || (isInvite && existingParticipants?.includes(user.userId))
                        }" @click="toggleUser(user)">
                            <div class="user-avatar">
                                {{ user.username[0] }}
                            </div>
                            <div class="user-info">
                                <div class="user-name">{{ user.username }}</div>
                                <div class="user-id">@{{ user.userId }}</div>
                            </div>
                            <div class="user-status">
                                <span v-if="isInvite && existingParticipants?.includes(user.userId)"
                                    class="already-joined">
                                    이미 참여 중
                                </span>
                                <div v-else class="checkbox-wrapper">
                                    <input type="checkbox" :id="user.userId" :value="user.userId"
                                        v-model="selectedUsers" :disabled="user.userId === props.currentUserId">
                                    <span class="checkmark"></span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button @click="$emit('close')" class="cancel-btn">취소</button>
                <button @click="handleSubmit" :disabled="!isValid" class="submit-btn"
                    :class="{ 'invite-btn': isInvite }">
                    {{ isInvite ? '초대하기' : '채팅방 만들기' }}
                </button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import axios from 'axios'

const props = defineProps({
    show: Boolean,
    currentUserId: String,
    isInvite: Boolean,
    existingParticipants: Array
})

const emit = defineEmits(['close', 'roomCreated', 'usersInvited'])

const roomName = ref('')
const users = ref([])
const selectedUsers = ref([])
const searchTerm = ref('')

const filteredUsers = computed(() => {
    return users.value.filter(user => {
        const searchLower = searchTerm.value.toLowerCase()
        return (user.username.toLowerCase().includes(searchLower) ||
            user.userId.toLowerCase().includes(searchLower))
    })
})

const isValid = computed(() => {
    if (props.isInvite) {
        return selectedUsers.value.length > 0
    }
    return roomName.value.trim() && selectedUsers.value.length > 0
})

const loadUsers = async () => {
    try {
        const response = await axios.get('http://localhost:8081/member')

        // id를 숫자로 유지
        users.value = response.data.map(user => ({
            userId: user.id,
            username: user.name
        })).filter(user => user.userId !== props.currentUserId)
    } catch (error) {
        console.error('사용자 목록 로딩 실패:', error)
    }
}

const handleSubmit = async () => {

    if (props.isInvite) {
        emit('usersInvited', selectedUsers.value)
    } else {
        try {

            const participants = [
                ...selectedUsers.value,
                props.currentUserId
            ]

            // API 요청 데이터 형식 수정
            const chatRoomData = {
                name: roomName.value,
                participants: participants.map(String) // 문자열 리스트로 변환
            }

            const response = await axios.post('http://localhost:8081/stomp/chatRoom/create', chatRoomData, {
                headers: {
                    'Content-Type': 'application/json'
                }
            })

            emit('roomCreated', response.data)
        } catch (error) {
            console.error('채팅방 생성 실패:', error)
            // 에러 상세 정보 출력
            if (error.response) {
                console.error('에러 응답:', error.response.data)
            }
        }
    }
    emit('close')
    resetForm()
}

const resetForm = () => {
    roomName.value = ''
    selectedUsers.value = []
    searchTerm.value = ''
}

watch(() => props.show, (newVal) => {
    if (newVal) {
        loadUsers()
    }
})

onMounted(() => {
    if (props.show) {
        loadUsers()
    }
})

const toggleUser = (user) => {
    if (user.userId === props.currentUserId ||
        (props.isInvite && existingParticipants?.includes(user.userId))) {
        return;
    }

    const index = selectedUsers.value.indexOf(user.userId);
    if (index === -1) {
        selectedUsers.value.push(user.userId);
    } else {
        selectedUsers.value.splice(index, 1);
    }
}
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
    backdrop-filter: blur(5px);
}

.modal-content {
    background: white;
    width: 90%;
    max-width: 500px;
    border-radius: 16px;
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
    overflow: hidden;
}

.modal-header {
    padding: 20px;
    background-color: #fff;
    border-bottom: 1px solid #eee;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.modal-header h3 {
    margin: 0;
    color: #333;
    font-size: 1.25rem;
    font-weight: 600;
}

.close-btn {
    background: none;
    border: none;
    font-size: 1.5rem;
    color: #666;
    cursor: pointer;
    padding: 0;
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    transition: background-color 0.2s;
}

.close-btn:hover {
    background-color: #f5f5f5;
}

.modal-body {
    padding: 20px;
}

.input-group {
    margin-bottom: 20px;
}

.input-group label {
    display: block;
    margin-bottom: 8px;
    color: #333;
    font-weight: 600;
    font-size: 0.95rem;
}

.input-wrapper {
    position: relative;
    display: flex;
    align-items: center;
}

.icon {
    position: absolute;
    left: 12px;
    color: #666;
    font-style: normal;
}

.room-name-input,
.search-input {
    width: 100%;
    padding: 12px 12px 12px 40px;
    border: 1px solid #ddd;
    border-radius: 12px;
    font-size: 0.95rem;
    transition: all 0.2s;
}

.room-name-input:focus,
.search-input:focus {
    border-color: #ffeb33;
    box-shadow: 0 0 0 3px rgba(255, 235, 51, 0.2);
    outline: none;
}

.selected-count {
    margin: 8px 0;
    color: #666;
    font-size: 0.9rem;
}

.user-list {
    max-height: 300px;
    overflow-y: auto;
    border: 1px solid #eee;
    border-radius: 12px;
    background: #f8f9fa;
}

.user-item {
    padding: 12px 16px;
    display: flex;
    align-items: center;
    gap: 12px;
    cursor: pointer;
    transition: all 0.2s;
}

.user-item:hover:not(.disabled) {
    background-color: #f0f0f0;
}

.user-item.selected {
    background-color: #fff9e6;
}

.user-item.disabled {
    opacity: 0.7;
    cursor: not-allowed;
}

.user-avatar {
    width: 40px;
    height: 40px;
    background-color: #ffeb33;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
    color: #333;
}

.user-info {
    flex: 1;
}

.user-name {
    font-weight: 600;
    color: #333;
}

.user-id {
    font-size: 0.85rem;
    color: #666;
}

.user-status {
    display: flex;
    align-items: center;
}

.already-joined {
    font-size: 0.85rem;
    color: #999;
    font-style: italic;
}

.checkbox-wrapper {
    position: relative;
    width: 20px;
    height: 20px;
}

.checkbox-wrapper input {
    opacity: 0;
    position: absolute;
}

.checkmark {
    position: absolute;
    top: 0;
    left: 0;
    width: 20px;
    height: 20px;
    border: 2px solid #ddd;
    border-radius: 4px;
    transition: all 0.2s;
}

.checkbox-wrapper input:checked+.checkmark {
    background-color: #ffeb33;
    border-color: #ffeb33;
}

.checkbox-wrapper input:checked+.checkmark:after {
    content: '✓';
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    color: #333;
    font-size: 14px;
}

.modal-footer {
    padding: 20px;
    border-top: 1px solid #eee;
    display: flex;
    justify-content: flex-end;
    gap: 12px;
}

.submit-btn,
.cancel-btn {
    padding: 12px 24px;
    border: none;
    border-radius: 12px;
    font-size: 0.95rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
}

.submit-btn {
    background-color: #ffeb33;
    color: #333;
}

.submit-btn:hover:not(:disabled) {
    background-color: #ffd700;
}

.submit-btn:disabled {
    background-color: #ddd;
    cursor: not-allowed;
}

.submit-btn.invite-btn {
    background-color: #4CAF50;
    color: white;
}

.submit-btn.invite-btn:hover:not(:disabled) {
    background-color: #45a049;
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
    width: 8px;
}

.user-list::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 4px;
}

.user-list::-webkit-scrollbar-thumb {
    background: #ddd;
    border-radius: 4px;
}

.user-list::-webkit-scrollbar-thumb:hover {
    background: #ccc;
}
</style>
