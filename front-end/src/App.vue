<template>
  <div id="app">
    <transition name="fade">
      <div v-if="showLoading" class="loading-overlay">
        <div class="loading-box">
          <div class="loading-spinner"></div>
          <p class="loading-text">잠시만 기다려주세요</p>
        </div>
      </div>
    </transition>

    <template v-if="isPublicLayout">
      <router-view />
    </template>
    <div v-else class="flex dashboard-layout">
      <SideBar />
      <div class="contents-wrap">
        <HeaderEle />
        <div class="contents">
          <router-view />
        </div>
        <FooterEle />
      </div>
    </div>

    <!-- 비밀번호 초기화 계정 로그인 시 강제 재설정 모달 (전역, 닫기 불가) -->
    <ChangePasswordModal
      v-if="authStore.mustChangePassword"
      @done="authStore.clearMustChangePassword()"
    />
  </div>
</template>

<script setup>
import { onMounted, computed, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useAuthStore } from '@/store/auth';
import SideBar from './components/SideBar.vue';
import FooterEle from "@/components/FooterEle.vue";
import HeaderEle from "@/components/HeaderEle.vue";
import ChangePasswordModal from './components/ChangePasswordModal.vue';

const authStore = useAuthStore();
const route = useRoute();

// 로그인·비밀번호 재설정 페이지: 사이드바/헤더 없이 전체 화면만 표시 (탈출 UI 제거)
const isPublicLayout = computed(() => !!route.meta?.isPublic || !!route.meta?.isResetPassword);

// 로딩 깜박임 방지: 250ms 후 표시, 한번 나타나면 최소 400ms 유지
const showLoading = ref(false);
let showTimer = null;
let hideTimer = null;
let shownAt = 0;

watch(() => authStore.isLoading, (loading) => {
  if (loading) {
    clearTimeout(hideTimer);
    showTimer = setTimeout(() => {
      showLoading.value = true;
      shownAt = Date.now();
    }, 250);
  } else {
    clearTimeout(showTimer);
    if (showLoading.value) {
      const elapsed = Date.now() - shownAt;
      const remaining = Math.max(0, 400 - elapsed);
      hideTimer = setTimeout(() => { showLoading.value = false; }, remaining);
    }
  }
});

onMounted(() => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    authStore.rehydrate();
  }
});
</script>

<style>
/* CSS 임포트 (전역 적용을 위해 scoped 제거 권장) */
@import "./style/flex.css";
@import "./style/reset.css";
@import "./style/common.css";

/* 레이아웃 구조 스타일 */
.dashboard-layout {
  width: 100%;
  min-height: 100vh;
}

.contents-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.contents {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  padding: 0; /* 필요에 따라 조절 */
  background-color: #f4f7f6;
}

/* 로딩 오버레이 */
.loading-overlay {
  position: fixed;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(3px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.loading-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

/* 원형 스피너 */
.loading-spinner {
  width: 44px;
  height: 44px;
  border: 3px solid #dbeafe;
  border-top-color: #2563eb;
  border-radius: 50%;
  animation: spin 0.75s linear infinite;
}

.loading-text {
  font-size: 0.82rem;
  color: #9ca3af;
  font-weight: 500;
  letter-spacing: 0.03em;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 페이드 트랜지션 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.15s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>