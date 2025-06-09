import { createRouter, createWebHistory } from 'vue-router'
import Login from '../components/Login.vue'
import Chat from '../components/Chat.vue'

const routes = [
    {
        path: '/',
        component: Login
    },
    
    {
    path: '/login',
    component: Login
    },
    {
        path: '/chat',
        component: Chat,
        meta: { requiresUserId: true }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 네비게이션 가드 수정
router.beforeEach((to, from, next) => {
    const userId = sessionStorage.getItem('memberId')
    
    if (to.matched.some(record => record.meta.requiresUserId)) {
        if (!userId) {
            next('/')
        } else {
            next()
        }
    } else {
        if (userId && to.path === '/') {
            next('/chat')
        } else {
            next()
        }
    }
})

export default router