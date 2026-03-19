// src/stores/auth.js
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export const useAuthStore = defineStore('auth', () => {
    // 1. 상태(State)
    const token = ref(localStorage.getItem('accessToken') || null);
    const user = ref((() => {
        try {
            const u = localStorage.getItem('userInfo');
            return u ? JSON.parse(u) : null;
        } catch { return null; }
    })());

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

    /** 새로고침 시 localStorage → 스토어 동기화 (mustChangePassword 유지) */
    function rehydrate() {
        const t = localStorage.getItem('accessToken');
        const u = localStorage.getItem('userInfo');
        token.value = t || null;
        try {
            user.value = u ? JSON.parse(u) : null;
        } catch {
            user.value = null;
        }
    }

    /** PASSWORD_RESET_YN === 'Y' 이면 true. App.vue 모달 표시 및 라우터 가드용. */
    const mustChangePassword = computed(() => user.value?.mustChangePassword === true);

    /** 비밀번호 재설정 완료 후 플래그 해제 */
    function clearMustChangePassword() {
        if (user.value) {
            user.value = { ...user.value, mustChangePassword: false };
            localStorage.setItem('userInfo', JSON.stringify(user.value));
        }
    }

    return {
        token,
        user,
        isLoading,
        setLoading,
        isAuthenticated,
        userName,
        userRole,
        mustChangePassword,
        login,
        logout,
        rehydrate,
        clearMustChangePassword
    };
});