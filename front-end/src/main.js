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

// 3. Axios 기본 설정 (백엔드 주소로 직접 요청)
axios.defaults.baseURL = 'http://localhost:8085';

// 세션 만료(401) 시 로그아웃 API는 한 번만 호출
let sessionExpiryHandling = false;

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
    authStore.setLoading(false);
    return response;
}, error => {
    authStore.setLoading(false);
    if (error.response?.status === 401) {
        if (!sessionExpiryHandling) {
            sessionExpiryHandling = true;
            const token = localStorage.getItem('accessToken');
            axios.post('/api/auth/logout', {}, {
                headers: token ? { Authorization: `Bearer ${token}` } : {}
            }).finally(() => {
                authStore.logout();
                sessionExpiryHandling = false;
                alert('세션이 만료되었습니다. 다시 로그인해주세요.');
                router.push('/login');
            });
        }
        // 이미 처리 중이면 로그아웃 API는 호출하지 않고 reject만 (리다이렉트·알림은 첫 번째만)
    }
    return Promise.reject(error);
});

// 전역 변수 설정 (선택 사항)
app.config.globalProperties.$axios = axios;

app.mount('#app');