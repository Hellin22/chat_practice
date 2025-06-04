import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import axios from 'axios'

// 기본 axios 설정
axios.defaults.baseURL = 'http://localhost:8080'

// 단순화된 인터셉터
axios.interceptors.request.use(
    config => {
        // userId를 헤더에 포함시키는 대신, 필요한 곳에서 파라미터로 전달
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 에러 처리 인터셉터
axios.interceptors.response.use(
    response => response,
    error => {
        // 일반적인 에러 처리
        console.error('API Error:', error);
        
        // 서버 연결 실패 등의 에러 처리
        if (!error.response) {
            console.error('Network/Server Error');
            // 필요한 경우 에러 페이지로 리다이렉트
            // router.push('/error');
        }
        
        return Promise.reject(error);
    }
)

const app = createApp(App)
app.use(router)

// axios를 전역적으로 사용할 수 있도록 설정
app.config.globalProperties.$axios = axios

app.mount('#app')