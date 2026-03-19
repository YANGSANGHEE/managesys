<template>
  <div id="app">
    <div v-if="authStore.isLoading" class="loading-overlay">
      <div class="loader"></div>
      <p> Loading...</p>
    </div>

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
import { onMounted, computed } from 'vue';
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

onMounted(() => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    // 로컬 스토리지에 토큰이 있다면 스토어 상태를 다시 로그인 상태로 변경
    authStore.rehydrate(); // 스토어에 미리 만들어둔 복구 함수 호출
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

/* 로딩창 스타일 */
.loading-overlay {
  position: fixed;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(255, 255, 255, 0.7);
  display: flex; flex-direction: column;
  justify-content: center; align-items: center;
  z-index: 9999; /* 모든 요소보다 위에 표시 */
}

.loader {
  border: 5px solid #f3f3f3;
  border-top: 5px solid #3d5afe;
  border-radius: 50%;
  width: 50px; height: 50px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>