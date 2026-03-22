<template>
  <aside class="sidebar">
    <div class="logo-area flex-center">
      <router-link to="/" class="logo"></router-link>
      <span class="company-name">더원컴퍼니</span>
    </div>

    <nav class="nav-menu">
      <!-- 공지사항 -->
      <router-link to="/notice" class="nav-item">
        <Bell :size="20" class="menu-icon" />
        <span>공지사항</span>
      </router-link>

      <!-- 고객관리 (토글: 클릭 시 접기/펼치기) -->
      <div class="nav-group">
        <button
          type="button"
          class="nav-group-title"
          :class="{ open: customersOpen }"
          @click="customersOpen = !customersOpen"
          aria-expanded="customersOpen"
        >
          <LayoutDashboard :size="20" class="menu-icon" />
          <span>고객관리</span>
          <ChevronDown :size="18" class="chevron" />
        </button>
        <div v-show="customersOpen" class="nav-group-inner">
          <router-link to="/customers/individual" class="nav-item sub">개인고객</router-link>
          <router-link to="/customers/partner" class="nav-item sub">협력점 고객</router-link>
        </div>
      </div>

      <!-- 인사관리 (최고관리자만) -->
      <router-link v-if="isAdmin" to="/userManagement" class="nav-item">
        <Users :size="20" class="menu-icon" />
        <span>인사관리</span>
      </router-link>

      <!-- 공통코드관리 (최고관리자, 매니저) -->
      <router-link v-if="isAdmin || isManager" to="/commonCode" class="nav-item">
        <Settings2 :size="20" class="menu-icon" />
        <span>공통코드관리</span>
      </router-link>
    </nav>
  </aside>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import { Bell, LayoutDashboard, Users, ChevronDown, Settings2 } from 'lucide-vue-next';
import { useAuthStore } from '@/store/auth';

const authStore = useAuthStore();
const isAdmin = computed(() => authStore.user?.userRole === 'ADMIN');
const isManager = computed(() => authStore.user?.userRole === 'MANAGER');

const customersOpen = ref(true);

// 고객관리 하위 페이지에 있으면 토글 열린 상태 유지
const route = useRoute();
watch(
  () => route.path,
  (path) => {
    if (path.startsWith('/customers')) customersOpen.value = true;
  },
  { immediate: true }
);
</script>

<style scoped>
/* 사이드바 전체 컨테이너 */
.sidebar {
  width: 260px;
  background-color: #fff;
  display: flex;
  flex-direction: column;
  padding: 24px;
  box-shadow: 4px 0 10px rgba(0,0,0,0.05);
  height: 100vh;
}

.logo-area {
  padding-bottom: 40px;
  flex-direction: column;
  gap: 8px;
}

.logo {
  display: block;
  width: 100px;
  height: 67px;
  background: url('../assets/logo.png') no-repeat center;
  background-size: contain;
  text-decoration: none;
}

.company-name {
  font-size: 0.95rem;
  font-weight: 700;
  color: #2563eb;
  letter-spacing: 0.03em;
}

.nav-menu {
  flex: 1;
}

/* 개별 메뉴 아이템 스타일 */
.nav-item {
  padding: 14px 18px;
  margin-bottom: 8px;
  border-radius: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #666;
  font-size: 14px;
  text-decoration: none;
  transition: all 0.3s ease;
  position: relative;
}

.nav-item:hover {
  background: #eff6ff;
  font-weight: bold;
}

.nav-item.sub {
  padding-left: 44px;
  font-size: 14px;
}

.nav-group {
  margin-bottom: 8px;
}

.nav-group-title {
  width: 100%;
  padding: 14px 18px;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #666;
  font-weight: 600;
  font-size: 14px;
  background: none;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  text-align: left;
  transition: background 0.2s;
}

.nav-group-title:hover {
  background: #eff6ff;
}

.nav-group-title .chevron {
  margin-left: auto;
  transition: transform 0.2s;
}

.nav-group-title.open .chevron {
  transform: rotate(180deg);
}

.nav-group-inner {
  overflow: hidden;
}

.nav-group .nav-item.sub.router-link-active {
  background: #2563eb !important;
  color: white !important;
  font-weight: 600;
}

/* Vue Router 활성화 클래스 (현재 페이지 강조) */
.router-link-active:not(.logo):not(.sub) {
  background: #2563eb !important;
  color: white !important;
  font-weight: 600;
}

.menu-icon {
  flex-shrink: 0;
}

.sidebar-footer {
  margin-top: auto;
}

.logout-btn {
  width: 100%;
  padding: 12px;
  background: #f8f9fa;
  border: 1px solid #eee;
  border-radius: 10px;
  cursor: pointer;
  color: #666;
  font-weight: 500;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: #ffebee;
  color: #d32f2f;
}
</style>