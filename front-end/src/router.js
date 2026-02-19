import { createRouter, createWebHistory } from 'vue-router'
import LoginView from './views/LoginView.vue'
import NoticeEle from './components/NoticeEle.vue'

const routes = [
    {
        path: '/',
        name: 'login',
        component: LoginView
    },
    {
        path: '/notice',
        name: 'Notice',
        component: NoticeEle
    }
]

const router = createRouter({
    history: createWebHistory(process.env.BASE_URL),
    routes
})

export default router