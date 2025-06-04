<template>
    <div class="chat-container">
        <div class="chat-header">
            <div class="header-content">
                <div class="room-controls">
                    <div class="room-selector">
                        <select id="roomSelect" v-model="currentRoom" @change="changeRoom">
                            <option v-for="room in rooms" :key="room.id" :value="room.id">
                                {{ room.name }} ({{ room.participants.join(', ') }})
                            </option>
                        </select>
                    </div>
                    <button class="create-room-btn" @click="showCreateRoom = true">
                        새 채팅방
                    </button>
                    <button class="leave-room-btn" @click="leaveCurrentRoom" v-if="currentRoom">
                        채팅방 나가기
                    </button>
                </div>
                <div class="user-info">
                    <span>{{ username }}</span>
                    <button class="logout-btn" @click="logout">로그아웃</button>
                </div>
            </div>
            <div class="connection-status" :class="{ 'connected': isConnected }">
                {{ connectionStatus }}
            </div>
        </div>

        <div class="chat-messages" ref="messageContainer">
            <div v-for="(message, index) in currentMessages" :key="index"
                :class="['message-wrapper', getMessageClass(message)]">
                <div class="message-info">
                    <span class="sender" v-if="shouldShowSender(message)">{{ message.sender }}</span>
                </div>
                <div class="message-bubble" :class="{ 'system-message': isSystemMessage(message) }">
                    {{ message.message }}
                </div>
                <div class="message-time">
                    {{ formatTime(message.timestamp) }}
                </div>
            </div>
        </div>

        <!-- 메시지 전송 부분 -->
        <div class="chat-input">
            <input v-model="newMessage" @keyup.enter="sendMessage" :disabled="!isConnected"
                placeholder="메시지를 입력하세요..." />
            <button @click="sendMessage" :disabled="!isConnected">전송</button>
        </div>

        <!-- 채팅방 생성 모달 -->
        <UserSelectModal :show="showCreateRoom" :currentUserId="memberId" :isInvite="false"
            @close="showCreateRoom = false" @roomCreated="handleRoomCreated" />
    </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'
import axios from 'axios'
import UserSelectModal from './UserSelectModal.vue'

const router = useRouter()
const memberId = ref(localStorage.getItem('memberId'))
const username = ref(localStorage.getItem('username') || '알 수 없음')
const newMessage = ref('')
const messagesPerRoom = ref({})
const isConnected = ref(false)
const stompClient = ref(null)
const connectionStatus = ref('웹소켓에 연결 중...')
const currentRoom = ref('room1')
const rooms = ref([])
const subscriptions = ref({})
const messageContainer = ref(null)
const showCreateRoom = ref(false)

const currentMessages = computed(() => messagesPerRoom.value[currentRoom.value] || [])

const formatTime = (timestamp) => {
    if (!timestamp) return ''
    const date = new Date(timestamp)
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

const scrollToBottom = async () => {
    await nextTick()
    if (messageContainer.value) {
        messageContainer.value.scrollTop = messageContainer.value.scrollHeight
    }
}

const loadPreviousMessages = async (roomId) => {
    try {
        const response = await axios.get(
            `http://localhost:8080/stomp/chat/${roomId}/message?memberId=${memberId.value}`
        );

        // 메시지 필터링 로직 강화
        messagesPerRoom.value[roomId] = response.data.filter(message => {
            // 시스템 메시지인 경우
            if (message.type === 'ENTER' || message.type === 'LEAVE') {
                return message.message &&
                    message.message.trim().length > 0 &&
                    message.message.includes('님이');
            }
            // 일반 채팅 메시지인 경우
            return message.message && message.message.trim().length > 0;
        });
        await scrollToBottom();
    } catch (error) {
        console.error('이전 메시지 로딩 실패:', error);
    }
}

const connectWebSocket = () => {
    console.log('웹 소켓 연결 시도 중...')
    connectionStatus.value = '웹 소켓에 연결 중...'

    const socket = new SockJS('http://localhost:8080/stomp-chat', null, {
        transports: ['websocket', 'xhr-streaming', 'xhr-polling'],          // websocket 지원하지 않을 시, 대체 방식
    })
    console.log('SockJS 인스턴스 생성됨')

    stompClient.value = new Client({
        webSocketFactory: () => socket,
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        debug: function (str) {
            console.log(str)
        },
        onConnect: frame => {
            console.log('STOMP 연결됨: ', frame)
            isConnected.value = true
            connectionStatus.value = '연결됨'

            // 사용자별 채팅방 목록 업데이트 구독 (async를 통해서 메시지가 도착했을 때, 실행되는 비동기 콜백 함수)
            stompClient.value.subscribe(`/topic/user/${memberId.value}/rooms/update`, async response => {

                const updatedRooms = JSON.parse(response.body);

                // 모든 채팅방 리스트에서 memberId가 포함된 채팅방만 도출
                rooms.value = updatedRooms.filter(room =>
                    room.participants.includes(memberId.value)
                );

                // 현재 선택된 방이 없고 채팅방이 있다면 첫 번째 방 선택
                if (!currentRoom.value && rooms.value.length > 0) {
                    currentRoom.value = rooms.value[0].id;
                    await connectToNewRoom();
                }
            });

            if (currentRoom.value) {
                subscribeToRoom(currentRoom.value);
            }
        },
        // 연결이 끊어졌을 때의 처리 추가
        onDisconnect: () => {
            console.log('STOMP 연결 해제됨')
            isConnected.value = false
            connectionStatus.value = '연결 끊김'
        }
    })

    console.log('STOMP 클라이언트 활성화 중...')
    stompClient.value.activate()
}

const connectToNewRoom = async () => {
    try {
        // 웹소켓 연결 확인 및 재연결
        if (!stompClient.value?.connected) {
            await connectWebSocket();
            // 웹소켓 연결 대기
            await new Promise((resolve) => {
                const checkConnection = setInterval(() => {
                    if (stompClient.value?.connected) {
                        clearInterval(checkConnection);
                        resolve();
                    }
                }, 100);
            });
        }

        // 연결된 상태에서 처리
        if (stompClient.value?.connected) {
            connectionStatus.value = '연결됨';
            
            // 현재 방의 구독만 정리
            if (subscriptions.value[currentRoom.value]) {
                subscriptions.value[currentRoom.value].unsubscribe();
                delete subscriptions.value[currentRoom.value];
            }
            
            // 현재 방의 업데이트 구독 정리
            if (subscriptions.value[`${currentRoom.value}-update`]) {
                subscriptions.value[`${currentRoom.value}-update`].unsubscribe();
                delete subscriptions.value[`${currentRoom.value}-update`];
            }

            // 새로운 구독 설정
            await Promise.all([
                loadPreviousMessages(currentRoom.value),
                new Promise(resolve => setTimeout(resolve, 100))
            ]);

            subscribeToRoom(currentRoom.value);

            // 메시지 컨테이너 스크롤
            await scrollToBottom();

            // 최초 입장 확인 및 메시지 전송
            const isFirst = await checkFirstJoin();
            if (isFirst) {
                await sendSystemMessage(currentRoom.value, 'ENTER');
            }

            // 채팅방 정보 업데이트 구독 설정
            subscriptions.value[`${currentRoom.value}-update`] = stompClient.value.subscribe(
                `/topic/room/${currentRoom.value}/update`,
                response => {
                    const updatedRoom = JSON.parse(response.body);
                    const index = rooms.value.findIndex(room => room.id === updatedRoom.id);
                    if (index !== -1) {
                        rooms.value[index] = updatedRoom;
                    }
                }
            );
        }
    } catch (error) {
        console.error('채팅방 연결 실패:', error);
        connectionStatus.value = '연결 실패';
    }
};

const changeRoom = async () => {
    if (stompClient.value && stompClient.value.connected) {
        if (subscriptions.value[currentRoom.value]) {
            subscriptions.value[currentRoom.value].unsubscribe();
            delete subscriptions.value[currentRoom.value];
        }

        await loadPreviousMessages(currentRoom.value);
        subscribeToRoom(currentRoom.value);

        // 최초 입장 여부 확인 로직 제거
        // 채팅방 변경 시에는 입장 메시지를 보내지 않음
    }
}

const subscribeToRoom = (roomId) => {
    if (subscriptions.value[roomId]) {
        console.log(`Already subscribed to room ${roomId}`);
        return;
    }

    // 채팅 메시지 구독
    subscriptions.value[roomId] = stompClient.value.subscribe(`/topic/message/${roomId}`, message => {
        console.log('메시지 수신:', message);
        if (!messagesPerRoom.value[roomId]) {
            messagesPerRoom.value[roomId] = [];
        }
        const messageData = JSON.parse(message.body);
        messagesPerRoom.value[roomId].push(messageData);
        scrollToBottom();
    });

    // 채팅방 정보 업데이트 구독
    subscriptions.value[`${roomId}-update`] = stompClient.value.subscribe(`/topic/room/${roomId}/update`, response => {
        const updatedRoom = JSON.parse(response.body);
        // 현재 채팅방 목록에서 해당 방 정보 업데이트
        const index = rooms.value.findIndex(room => room.id === updatedRoom.id);
        if (index !== -1) {
            rooms.value[index] = updatedRoom;
        }
    });
}

const logout = async () => {
    try {
        if (stompClient.value) {
            Object.values(subscriptions.value).forEach(subscription => subscription.unsubscribe())
            await stompClient.value.deactivate()
        }

        localStorage.removeItem('memberId')
        localStorage.removeItem('username')

        router.push('/')
    } catch (error) {
        console.error('로그아웃 중 에러 발생:', error)
    }
}

const sendMessage = () => {

    if (newMessage.value && isConnected.value) {
        const chatMessage = {
            roomId: currentRoom.value,
            sender: username.value,    // 실제 로그인한 사용자 이름 사용
            message: newMessage.value,
            type: 'CHAT'
        }

        console.log('메시지 전송:', chatMessage)
        stompClient.value.publish({
            destination: `/app/${currentRoom.value}`,
            body: JSON.stringify(chatMessage)
        })
        newMessage.value = ''
    } else if (!isConnected.value) {
        console.error('웹소켓에 연결되지 않았습니다')
        connectionStatus.value = '메시지를 보낼 수 없습니다. 연결 중...'
    }
}

const loadRooms = async () => {
    try {
        const response = await axios.get('http://localhost:8080/stomp/chatRoom')

        // 사용자가 참여한 채팅방만 필터링
        rooms.value = response.data.filter(room =>
            room.participants.includes(memberId.value)
        )

        // 사용자가 참여한 채팅방이 있다면 첫 번째 방을 현재 방으로 설정
        if (rooms.value && rooms.value.length > 0) {
            currentRoom.value = rooms.value[0].id
        } else {
            connectionStatus.value = '참여 중인 채팅방이 없습니다.'
        }
    } catch (error) {
        console.error('채팅방 목록 로딩 실패:', error)
        connectionStatus.value = '채팅방 목록을 불러올 수 없습니다.'
    }
}

const handleRoomCreated = async (newRoom) => {
    try {
        await loadRooms();
        currentRoom.value = newRoom.id;
        await connectToNewRoom();
    } catch (error) {
        console.error('채팅방 생성 후 처리 실패:', error);
    }
};

const getMessageClass = (message) => {
    if (isSystemMessage(message)) return 'system-message-wrapper'
    return message.sender === username.value ? 'my-message' : 'other-message'
}

const shouldShowSender = (message) => {
    return !isSystemMessage(message) && message.sender !== username.value
}

const isSystemMessage = (message) => {
    return message.type === 'ENTER' || message.type === 'LEAVE'
}

const sendSystemMessage = (roomId, type) => {


    const message = type === 'ENTER' ?
        `${username.value}님이 입장하셨습니다.` :
        `${username.value}님이 퇴장하셨습니다.`

    if (message) {
        const systemMessage = {
            roomId: roomId,
            sender: username.value,
            message: message,
            type: type
        }

        stompClient.value.publish({
            destination: `/app/${roomId}`,
            body: JSON.stringify(systemMessage)
        })
    }
}

const leaveCurrentRoom = async () => {
    if (!currentRoom.value) return;

    try {

        console.log("currentRoom", currentRoom.value);
        // 퇴장 메시지 전송
        await sendSystemMessage(currentRoom.value, 'LEAVE');

        // 서버에 채팅방 나가기 요청
        await axios.post(`http://localhost:8080/stomp/chatRoom/${currentRoom.value}/leave?memberId=${memberId.value}`);

        // 구독 해제
        if (subscriptions.value[currentRoom.value]) {
            subscriptions.value[currentRoom.value].unsubscribe();
            delete subscriptions.value[currentRoom.value];
        }

        // 채팅방 목록 새로고침
        await loadRooms();

        // 남은 채팅방이 있으면 첫 번째 방으로 ��동
        if (rooms.value.length > 0) {
            currentRoom.value = rooms.value[0].id;
            await changeRoom();
        } else {
            currentRoom.value = null;
            messagesPerRoom.value = {};
            connectionStatus.value = '참여 중인 채팅방이 없습니다.';
        }
    } catch (error) {
        console.error('채팅방 나가기 실패:', error);
    }
}

const currentRoomParticipants = computed(() => {
    const currentRoomData = rooms.value.find(room => room.id === currentRoom.value)
    return currentRoomData?.participants || []
})

// 최초 입장 여부 확인 함수 추가
const checkFirstJoin = async () => {
    try {
        const response = await axios.get(
            `http://localhost:8080/stomp/chat/${currentRoom.value}/isFirstJoin?memberId=${memberId.value}`
        );
        return response.data;
    } catch (error) {
        console.error('입장 이력 확인 실패:', error);
        return false;
    }
};

onMounted(async () => {
    if (!memberId.value) {
        router.push('/');
        return;
    }

    try {
        await loadRooms();
        if (rooms.value && rooms.value.length > 0) {
            await connectWebSocket();
            // connectWebSocket 완료 후 약간의 지연을 주고 메시지 로드
            await new Promise(resolve => setTimeout(resolve, 500));
            await loadPreviousMessages(currentRoom.value);
        } else {
            connectionStatus.value = '참여 중인 채팅방이 없습니다.';
        }
    } catch (error) {
        console.error('초기화 실패:', error);
        connectionStatus.value = '연결 실패';
        if (error.response?.status === 401) {
            router.push('/');
        }
    }
});

onUnmounted(async () => {
    if (stompClient.value) {
        try {
            if (stompClient.value.connected && currentRoom.value) {
                // 퇴장 메시지 전송
                await sendSystemMessage(currentRoom.value, 'LEAVE')
            }

            // 구독 해제
            Object.values(subscriptions.value).forEach(subscription => {
                if (subscription && subscription.unsubscribe) {
                    subscription.unsubscribe()
                }
            })

            // STOMP 클라이언트 비활성화
            if (stompClient.value.deactivate) {
                await stompClient.value.deactivate()
            }
        } catch (error) {
            console.error('연결 해제 중 오류 발생:', error)
        }
    }
})
</script>

<style scoped>
.chat-container {
    max-width: 800px;
    margin: 0 auto;
    height: 100vh;
    display: flex;
    flex-direction: column;
    background-color: #f5f6f8;
}

.chat-header {
    padding: 15px 20px;
    background-color: #ffeb33;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
    margin-bottom: 8px;
}

.room-controls {
    display: flex;
    gap: 1rem;
    align-items: center;
    flex: 1;
}

.room-selector {
    flex: 1;
    max-width: 300px;
}

.room-selector select {
    width: 100%;
    padding: 8px 12px;
    border-radius: 5px;
    border: 1px solid #ddd;
    background-color: white;
    font-size: 0.9rem;
}

.create-room-btn {
    padding: 8px 16px;
    background-color: white;
    border: 1px solid #ddd;
    border-radius: 4px;
    cursor: pointer;
    font-weight: bold;
    white-space: nowrap;
}

.create-room-btn:hover {
    background-color: #f5f5f5;
}

.user-info {
    display: flex;
    align-items: center;
    gap: 1rem;
    margin-left: 20px;
}

.user-info span {
    color: #333;
    font-weight: bold;
    white-space: nowrap;
}

.logout-btn {
    padding: 6px 12px;
    background-color: white;
    border: 1px solid #ddd;
    border-radius: 4px;
    cursor: pointer;
    font-size: 0.9rem;
    white-space: nowrap;
}

.logout-btn:hover {
    background-color: #f5f5f5;
}

.connection-status {
    font-size: 0.8em;
    color: #666;
    text-align: right;
    padding-top: 4px;
}

.connection-status.connected {
    color: #2c8a2c;
}

.chat-messages {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
    display: flex;
    flex-direction: column;
}

.message-wrapper {
    margin: 10px 0;
    max-width: 70%;
}

.my-message {
    align-self: flex-end;
}

.other-message {
    align-self: flex-start;
}

.message-info {
    margin-bottom: 5px;
}

.sender {
    font-size: 0.9em;
    color: #666;
}

.message-bubble {
    padding: 10px 15px;
    border-radius: 15px;
    background-color: white;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
    word-break: break-word;
}

.my-message .message-bubble {
    background-color: #ffeb33;
}

.message-time {
    font-size: 0.8em;
    color: #999;
    margin-top: 5px;
    text-align: right;
}

.chat-input {
    padding: 15px;
    background-color: white;
    display: flex;
    gap: 10px;
    border-top: 1px solid #ddd;
}

.chat-input input {
    flex: 1;
    padding: 12px 16px;
    border: 1px solid #ddd;
    border-radius: 20px;
    outline: none;
    font-size: 0.95rem;
}

.chat-input button {
    padding: 10px 24px;
    background-color: #ffeb33;
    border: none;
    border-radius: 20px;
    cursor: pointer;
    font-weight: bold;
    font-size: 0.95rem;
}

.chat-input button:hover {
    background-color: #ffd700;
}

.chat-input button:disabled {
    background-color: #ddd;
    cursor: not-allowed;
}

.system-message-wrapper {
    align-self: center;
    max-width: 90%;
}

.system-message {
    background-color: #f0f0f0 !important;
    color: #666;
    font-size: 0.9em;
    padding: 8px 16px;
    border-radius: 20px;
    text-align: center;
}

.leave-room-btn {
    padding: 8px 16px;
    background-color: #ff4444;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-weight: bold;
    white-space: nowrap;
}

.leave-room-btn:hover {
    background-color: #ff3333;
}

.invite-btn {
    padding: 8px 16px;
    background-color: #4CAF50;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-weight: bold;
    white-space: nowrap;
}

.invite-btn:hover {
    background-color: #45a049;
}
</style>