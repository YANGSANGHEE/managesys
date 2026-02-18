import { createApp } from 'vue'
import { createPinia } from 'pinia' // 추가
import App from './App.vue'
import router from './router'
import axios from 'axios' // axios 추가

// 1. Axios 기본 URL 설정 (백엔드 주소)
axios.defaults.baseURL = 'http://localhost:8085'; // application.properties 포트 8085 확인

// 2. 요청 인터셉터 설정 (모든 요청 전에 실행됨)
axios.interceptors.request.use(config => {
    const token = localStorage.getItem('accessToken');
    if (token) {
        // 토큰이 있으면 헤더에 'Bearer 토큰값' 형식으로 추가
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

const app = createApp(App);
app.config.globalProperties.$axios = axios; // 전역 사용 설정 (선택사항)
app.use(createPinia());
app.use(router).mount('#app');