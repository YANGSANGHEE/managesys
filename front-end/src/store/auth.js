// src/stores/auth.js
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export const useAuthStore = defineStore('auth', () => {
    // 1. 상태(State)
    const token = ref(localStorage.getItem('accessToken') || null);
    const user = ref(JSON.parse(localStorage.getItem('userInfo')) || null);

    // ▼ 로딩 상태 추가
    const isLoading = ref(false);

    function setLoading(status) {
        isLoading.value = status;
    }

    // 2. 게터(Getters)
    const isAuthenticated = computed(() => !!token.value);
    const userName = computed(() => user.value ? user.value.userName : '');
    const userRole = computed(() => user.value ? user.value.userRole : '');

    // 3. 액션(Actions)
    function login(accessToken, userInfo) {
        token.value = accessToken;
        user.value = userInfo;
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('userInfo', JSON.stringify(userInfo));
    }

    function logout() {
        token.value = null;
        user.value = null;
        localStorage.removeItem('accessToken');
        localStorage.removeItem('userInfo');
    }

    // [중요] 외부에서 쓸 수 있도록 isLoading과 setLoading을 추가합니다.
    return {
        token,
        user,
        isLoading,     // 추가
        setLoading,   // 추가
        isAuthenticated,
        userName,
        userRole,
        login,
        logout
    };
});