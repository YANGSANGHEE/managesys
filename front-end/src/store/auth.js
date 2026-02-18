// src/stores/auth.js
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export const useAuthStore = defineStore('auth', () => {
    // 1. 상태(State): 초기값은 localStorage에서 가져옴 (새로고침 대비)
    const token = ref(localStorage.getItem('accessToken') || null);
    const user = ref(JSON.parse(localStorage.getItem('userInfo')) || null);

    // 2. 게터(Getters): 상태를 가공해서 쓸 때
    const isAuthenticated = computed(() => !!token.value); // 로그인 여부 T/F
    const userName = computed(() => user.value ? user.value.userName : ''); // 이름만 편하게 가져오기
    const userRole = computed(() => user.value ? user.value.userRole : ''); // 권한 가져오기

    // 3. 액션(Actions): 로그인/로그아웃 함수
    function login(accessToken, userInfo) {
        token.value = accessToken;
        user.value = userInfo;

        // 브라우저 저장소에도 저장 (새로고침 해도 유지되게)
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('userInfo', JSON.stringify(userInfo));
    }

    function logout() {
        token.value = null;
        user.value = null;

        localStorage.removeItem('accessToken');
        localStorage.removeItem('userInfo');

        // 로그아웃 후 메인이나 로그인 페이지로 이동시키는 로직을 컴포넌트에서 처리
    }

    return { token, user, isAuthenticated, userName, userRole, login, logout };
});