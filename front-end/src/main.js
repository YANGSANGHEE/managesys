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

// 3. Axios 기본 설정 (상대 경로 - 프론트/백엔드 동일 서버 서빙)
// axios.defaults.baseURL = 'http://43.203.193.217:10000'; // 로컬 개발 시만 사용
axios.defaults.baseURL = '';

// 세션 만료(401) 시 로그아웃 API는 한 번만 호출
let sessionExpiryHandling = false;
// 동시 요청 카운터: 모든 요청이 끝났을 때만 로딩 해제
let pendingRequests = 0;

/** JWT exp claim으로 만료 여부 확인 (서명 검증 없이) */
function isTokenExpired(token) {
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        return Date.now() >= payload.exp * 1000;
    } catch {
        return true;
    }
}

/** 세션 만료 공통 처리 (alert + 로그아웃 + 리다이렉트) */
function handleSessionExpiry() {
    if (sessionExpiryHandling) return;
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

// [보강 1] 1분마다 토큰 만료 체크 - 페이지에 오래 머물러도 감지
setInterval(() => {
    const token = localStorage.getItem('accessToken');
    if (token && isTokenExpired(token) && authStore.isAuthenticated) {
        handleSessionExpiry();
    }
}, 60000);

// [보강 2] 탭 간 로그아웃 동기화 - 한 탭에서 로그아웃 시 모든 탭 로그아웃
window.addEventListener('storage', (e) => {
    if (e.key === 'accessToken' && !e.newValue && authStore.isAuthenticated) {
        authStore.logout();
        router.push('/login');
    }
});

// 4. Axios 인터셉터 통합 설정
axios.interceptors.request.use(config => {
    pendingRequests++;
    authStore.setLoading(true);

    // 토큰 추가
    const token = localStorage.getItem('accessToken');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
}, error => {
    pendingRequests = Math.max(0, pendingRequests - 1);
    if (pendingRequests === 0) authStore.setLoading(false);
    return Promise.reject(error);
});

axios.interceptors.response.use(response => {
    pendingRequests = Math.max(0, pendingRequests - 1);
    if (pendingRequests === 0) authStore.setLoading(false);
    return response;
}, error => {
    pendingRequests = Math.max(0, pendingRequests - 1);
    if (pendingRequests === 0) authStore.setLoading(false);
    if (error.response?.status === 401) {
        handleSessionExpiry();
    }
    return Promise.reject(error);
});

// 전역 변수 설정 (선택 사항)
app.config.globalProperties.$axios = axios;

app.mount('#app');