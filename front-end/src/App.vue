<template>
  <div id="app">
    <div v-if="authStore.isLoading" class="loading-overlay">
      <div class="loader"></div>
      <p> Loading...</p>
    </div>

    <router-view v-if="$route.meta.isPublic" />

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
  </div>
</template>

<script setup>
import { useAuthStore } from '@/store/auth';
import SideBar from './components/SideBar.vue';
import FooterEle from "@/components/FooterEle.vue";
import HeaderEle from "@/components/HeaderEle.vue";

// Pinia 스토어 호출
const authStore = useAuthStore();
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