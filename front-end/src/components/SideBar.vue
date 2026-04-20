<template>
  <aside class="sidebar" :class="{ collapsed }">
    <div class="logo-area">
      <router-link to="/" class="logo" :title="collapsed ? '더원컴퍼니' : ''"></router-link>
      <span v-if="!collapsed" class="company-name">더원컴퍼니</span>
      <button
        type="button"
        class="collapse-btn"
        @click="toggleCollapsed"
        :title="collapsed ? '사이드바 펼치기' : '사이드바 접기'"
        :aria-label="collapsed ? '사이드바 펼치기' : '사이드바 접기'"
      >
        <PanelLeftClose v-if="!collapsed" :size="18" />
        <PanelLeftOpen v-else :size="18" />
      </button>
    </div>

    <nav class="nav-menu">
      <!-- 공지사항 -->
      <router-link to="/notice" class="nav-item" :title="collapsed ? '공지사항' : ''">
        <Bell :size="20" class="menu-icon" />
        <span v-if="!collapsed">공지사항</span>
      </router-link>

      <!-- 고객관리 (토글: 클릭 시 접기/펼치기) -->
      <div class="nav-group">
        <button
          type="button"
          class="nav-group-title"
          :class="{ open: customersOpen && !collapsed, active: isCustomersActive }"
          @click="onCustomersClick"
          :title="collapsed ? '고객관리' : ''"
          :aria-expanded="customersOpen && !collapsed"
        >
          <LayoutDashboard :size="20" class="menu-icon" />
          <span v-if="!collapsed">고객관리</span>
          <ChevronDown v-if="!collapsed" :size="18" class="chevron" />
        </button>
        <div v-show="customersOpen && !collapsed" class="nav-group-inner">
          <router-link to="/customers/individual" class="nav-item sub">개인고객</router-link>
        </div>
      </div>

      <!-- 인사관리 (최고관리자만) -->
      <router-link
        v-if="isAdmin"
        to="/userManagement"
        class="nav-item"
        :title="collapsed ? '인사관리' : ''"
      >
        <Users :size="20" class="menu-icon" />
        <span v-if="!collapsed">인사관리</span>
      </router-link>

      <!-- 공통코드관리 (최고관리자, 매니저) -->
      <router-link
        v-if="isAdmin || isManager"
        to="/commonCode"
        class="nav-item"
        :title="collapsed ? '공통코드관리' : ''"
      >
        <Settings2 :size="20" class="menu-icon" />
        <span v-if="!collapsed">공통코드관리</span>
      </router-link>
    </nav>
  </aside>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  Bell,
  LayoutDashboard,
  Users,
  ChevronDown,
  Settings2,
  PanelLeftClose,
  PanelLeftOpen,
} from 'lucide-vue-next';
import { useAuthStore } from '@/store/auth';

const authStore = useAuthStore();
const isAdmin = computed(() => authStore.user?.userRole === 'ADMIN');
const isManager = computed(() => authStore.user?.userRole === 'MANAGER');

const COLLAPSE_KEY = 'sidebar.collapsed';
const collapsed = ref(localStorage.getItem(COLLAPSE_KEY) === '1');

const toggleCollapsed = () => {
  collapsed.value = !collapsed.value;
  localStorage.setItem(COLLAPSE_KEY, collapsed.value ? '1' : '0');
  // 레이아웃 변경 후 AG Grid 등 리사이즈가 필요한 컴포넌트를 위해 이벤트 발행
  window.dispatchEvent(new Event('resize'));
};

const customersOpen = ref(true);
const route = useRoute();
const router = useRouter();
const isCustomersActive = computed(() => route.path.startsWith('/customers'));

// 고객관리 클릭: 접힘 상태면 펼치며 기본 경로로 이동
const onCustomersClick = () => {
  if (collapsed.value) {
    collapsed.value = false;
    localStorage.setItem(COLLAPSE_KEY, '0');
    customersOpen.value = true;
    if (!route.path.startsWith('/customers')) {
      router.push('/customers/individual');
    }
    window.dispatchEvent(new Event('resize'));
    return;
  }
  customersOpen.value = !customersOpen.value;
};

// 고객관리 하위 페이지에 있으면 토글 열린 상태 유지
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
  transition: width 0.2s ease, padding 0.2s ease;
  flex-shrink: 0;
}

.sidebar.collapsed {
  width: 72px;
  padding: 24px 10px;
}

.logo-area {
  position: relative;
  padding-bottom: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.logo {
  display: block;
  width: 100px;
  height: 67px;
  background: url('../assets/logo.png') no-repeat center;
  background-size: contain;
  text-decoration: none;
  transition: width 0.2s ease, height 0.2s ease;
}

.sidebar.collapsed .logo {
  width: 40px;
  height: 40px;
}

.company-name {
  font-size: 0.95rem;
  font-weight: 700;
  color: #2563eb;
  letter-spacing: 0.03em;
}

/* 접기/펼치기 버튼 */
.collapse-btn {
  position: absolute;
  top: -6px;
  right: -10px;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 50%;
  cursor: pointer;
  color: #4b5563;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
  transition: background 0.15s, color 0.15s;
  padding: 0;
}
.collapse-btn:hover {
  background: #eff6ff;
  color: #2563eb;
}

.sidebar.collapsed .collapse-btn {
  right: 50%;
  transform: translateX(50%);
  top: auto;
  position: static;
  margin-top: 8px;
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
  transition: all 0.2s ease;
  position: relative;
  white-space: nowrap;
  overflow: hidden;
}

.nav-item:hover {
  background: #eff6ff;
  font-weight: bold;
}

.nav-item.sub {
  padding-left: 44px;
  font-size: 14px;
}

/* 접힘 상태: 아이콘만 중앙 정렬 */
.sidebar.collapsed .nav-item,
.sidebar.collapsed .nav-group-title {
  padding: 12px 0;
  justify-content: center;
  gap: 0;
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
  white-space: nowrap;
  overflow: hidden;
}

.nav-group-title:hover {
  background: #eff6ff;
}

/* 접힘 상태에서 고객관리 하위 경로에 있으면 그룹 타이틀도 강조 */
.sidebar.collapsed .nav-group-title.active {
  background: #2563eb;
  color: white;
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
