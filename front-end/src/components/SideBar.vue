<template>
  <aside class="sidebar">
    <div class="logo-area flex-center">
      <router-link to="/" class="logo"></router-link>
    </div>

    <nav class="nav-menu">
      <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
      >
        <component :is="item.icon" :size="20" class="menu-icon" />
        <span>{{ item.label }}</span>
      </router-link>
    </nav>
  </aside>
</template>

<script setup>
import {
  Bell,
  LayoutDashboard,
  Target,
  Users,
  LifeBuoy,
  Wallet,
  PlusCircle
} from 'lucide-vue-next';

const menuItems = [
  { path: '/notice', label: '공지사항', icon: Bell },
  { path: '/customers', label: '고객관리', icon: LayoutDashboard },
  { path: '/targets', label: '가망고객', icon: Target },
  { path: '/hr', label: '인사관리', icon: Users },
  { path: '/support', label: '고객센터', icon: LifeBuoy },
  { path: '/settlement', label: '정산관리', icon: Wallet },
  { path: '/extra', label: '부가서비스', icon: PlusCircle },
];
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
}

.logo {
  display: block;
  width: 100px;
  height: 67px;
  background: url('../assets/logo.png') no-repeat center;
  background-size: contain;
  text-decoration: none;
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
  text-decoration: none;
  transition: all 0.3s ease;
  position: relative;
}

.nav-item:hover {
  background: #f0f4ff;
  font-weight: bold;
}

/* Vue Router 활성화 클래스 (현재 페이지 강조) */
.router-link-active:not(.logo) {
  background: #3d5afe !important;
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