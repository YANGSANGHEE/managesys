import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import axios from 'axios'
import { useAuthStore } from '@/store/auth';

const app = createApp(App);
const pinia = createPinia();

app.use(pinia); // 1. Pinia를 먼저 앱에 등록
app.use(router);

// 2. Pinia 등록 후 스토어 가져오기 (에러 방지)
const authStore = useAuthStore();

// 3. Axios 기본 설정
axios.defaults.baseURL = 'http://localhost:8085';

// 4. Axios 인터셉터 통합 설정
axios.interceptors.request.use(config => {
    // 로딩 시작
    authStore.setLoading(true);

    // 토큰 추가
    const token = localStorage.getItem('accessToken');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
}, error => {
    authStore.setLoading(false);
    return Promise.reject(error);
});

axios.interceptors.response.use(response => {
    // 응답 성공 시 로딩 종료
    authStore.setLoading(false);
    return response;
}, error => {
    // 응답 에러 시 로딩 종료
    authStore.setLoading(false);
    return Promise.reject(error);
});

// 전역 변수 설정 (선택 사항)
app.config.globalProperties.$axios = axios;

app.mount('#app');